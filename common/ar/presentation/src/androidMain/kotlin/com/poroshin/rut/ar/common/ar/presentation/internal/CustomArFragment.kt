package com.poroshin.rut.ar.common.ar.presentation.internal

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.poroshin.rut.ar.common.ar.domain.ArCollisionEvaluator
import com.poroshin.rut.ar.common.ar.domain.ArDimensionValidator
import com.poroshin.rut.ar.common.ar.domain.ArModelDimensionsMeters
import com.poroshin.rut.ar.common.ar.domain.ArModelDimensionsMm
import com.poroshin.rut.ar.common.ar.domain.ArObjectParams
import com.poroshin.rut.ar.common.ar.domain.ArPlacementPolicy
import com.poroshin.rut.ar.common.ar.domain.ArPlaneType
import com.poroshin.rut.ar.common.ar.domain.ArScaleCalculator
import com.poroshin.rut.ar.common.ar.domain.ArValidationResult
import com.poroshin.rut.ar.common.ar.presentation.toArObjectParams
import com.poroshin.rut.ar.common.ar.presentation.toBundle
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt
import kotlinx.coroutines.launch

class CustomArFragment : ArFragment(), Scene.OnUpdateListener {

    private lateinit var params: ArObjectParams
    private lateinit var controller: ArSceneController

    private var modelRenderable: ModelRenderable? = null
    private var gestureDetector: GestureDetector? = null
    private var draggingNode: TransformableNode? = null
    private var singleMode: Boolean = true
    private var modelScale: Vector3 = Vector3.one()

    private val placedNodes = mutableListOf<TransformableNode>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        controller = ViewModelProvider(requireParentFragment())[ArSceneController::class.java]
        val availability = ArCoreApk.getInstance().checkAvailability(context)
        if (!availability.isSupported) {
            controller.reportError(ArSceneController.SceneError.ArNotAvailable)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        params = requireArguments().toArObjectParams()
            ?: error("ArObjectParams must be provided")
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): android.view.View? = super.onCreateView(inflater, container, savedInstanceState)

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instructionsController.setEnabled(false)
        instructionsController.setVisible(false)

        if (!validateModelDimensions()) {
            return
        }

        singleMode = controller.singleMode.value

        observeControllerCommands()
        setupGestureDetector()
        setupSceneListeners()
        loadModel(force = modelRenderable == null)
    }

    override fun onResume() {
        super.onResume()
        arSceneView.scene.addOnUpdateListener(this)
    }

    override fun onPause() {
        super.onPause()
        arSceneView.scene.removeOnUpdateListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        draggingNode = null
        gestureDetector = null
    }

    override fun onCreateSessionConfig(session: Session): Config {
        return super.onCreateSessionConfig(session).apply {
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        }
    }

    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView.arFrame ?: return
        controller.reportTracking(resolveTrackingStatus(frame))
    }

    private fun resolveTrackingStatus(frame: Frame): ArSceneController.TrackingStatus {
        val cameraTracking = frame.camera.trackingState
        return when (cameraTracking) {
            TrackingState.TRACKING -> {
                val hasSuitablePlane = arSceneView.session?.getAllTrackables(Plane::class.java)
                    ?.any { it.trackingState == TrackingState.TRACKING && planeMatchesPlacement(it) } == true
                if (hasSuitablePlane) {
                    ArSceneController.TrackingStatus.Tracking
                } else {
                    ArSceneController.TrackingStatus.Searching
                }
            }
            TrackingState.PAUSED,
            TrackingState.STOPPED -> ArSceneController.TrackingStatus.Lost
        }
    }

    private fun observeControllerCommands() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    controller.singleMode.collect { mode ->
                        singleMode = mode
                        if (mode && placedNodes.size > 1) {
                            val survivor = placedNodes.last()
                            val toRemove = placedNodes.dropLast(1)
                            toRemove.forEach { removeNode(it) }
                            placedNodes.retainAll(listOf(survivor))
                            controller.reportPlacedCount(placedNodes.size)
                        }
                    }
                }
                launch {
                    controller.clearAllRequests.collect {
                        clearAllNodes()
                    }
                }
                launch {
                    controller.reloadRequests.collect {
                        reloadModel()
                    }
                }
            }
        }
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                val hitTest = arSceneView.scene.hitTest(e, true)
                val hitNode = when (val node = hitTest.node) {
                    is TransformableNode -> node
                    else -> node?.parent as? TransformableNode
                }
                if (hitNode != null) {
                    draggingNode = hitNode
                    transformationSystem.selectNode(hitNode)
                }
            }
        })
    }

    private fun setupSceneListeners() {
        arSceneView.scene.addOnPeekTouchListener { hitTestResult, motionEvent ->
            transformationSystem.onTouch(hitTestResult, motionEvent)
            gestureDetector?.onTouchEvent(motionEvent)

            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_MOVE -> handleDrag(motionEvent)
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> draggingNode = null
            }
        }

        setOnTapArPlaneListener { hit, plane, _ ->
            if (!planeMatchesPlacement(plane)) {
                controller.reportError(ArSceneController.SceneError.PlaneNotAllowed)
                return@setOnTapArPlaneListener
            }
            placeModel(hit)
        }
    }

    private fun handleDrag(event: MotionEvent) {
        val node = draggingNode ?: return
        if (event.pointerCount > 1) return
        val frame = arSceneView.arFrame ?: return
        val hits = frame.hitTest(event)
        for (result in hits) {
            val trackable = result.trackable as? Plane ?: continue
            if (!trackable.isPoseInPolygon(result.hitPose)) continue
            if (!planeMatchesPlacement(trackable)) continue

            val newAnchor = createClampedAnchor(result)
            val newParent = AnchorNode(newAnchor).apply { setParent(arSceneView.scene) }
            val previousParent = node.parent as? AnchorNode

            node.parent = newParent
            if (hasIntersection(node, skip = node)) {
                node.parent = previousParent
                newAnchor.detach()
                newParent.setParent(null)
                controller.reportError(ArSceneController.SceneError.Collision)
            } else {
                previousParent?.anchor?.detach()
                previousParent?.setParent(null)
                controller.reportError(null)
                Log.d(TAG, "Model moved to ${anchorPoseToString(newAnchor)}")
            }
            break
        }
    }

    private fun placeModel(hit: HitResult) {
        if (!validateModelDimensions()) return
        val model = modelRenderable ?: run {
            controller.reportError(ArSceneController.SceneError.ModelLoadingFailed)
            return
        }

        if (singleMode && placedNodes.isNotEmpty()) {
            placedNodes.toList().forEach { removeNode(it) }
            placedNodes.clear()
        }

        val anchor = createClampedAnchor(hit)
        val anchorNode = AnchorNode(anchor).apply {
            setParent(arSceneView.scene)
        }
        val node = TransformableNode(transformationSystem).apply {
            scaleController.isEnabled = false
            translationController.isEnabled = false
            rotationController.isEnabled = true
            renderable = model
            setParent(anchorNode)
            localScale = modelScale
        }

        if (hasIntersection(node, skip = null)) {
            anchorNode.setParent(null)
            controller.reportError(ArSceneController.SceneError.Collision)
            return
        }

        placedNodes.add(node)
        controller.reportError(null)
        controller.reportPlacedCount(placedNodes.size)
        node.select()
        Log.d(TAG, "Model placed at ${anchorPoseToString(anchor)}")
    }

    private fun hasIntersection(node: TransformableNode, skip: TransformableNode?): Boolean {
        val targetShape = node.collisionShape as? Box ?: return false
        val targetAabb = computeWorldAabb(node, targetShape) ?: return false

        for (other in placedNodes) {
            if (other === node || other === skip) continue
            val otherShape = other.collisionShape as? Box ?: continue
            val otherAabb = computeWorldAabb(other, otherShape) ?: continue
            if (ArCollisionEvaluator.intersects(targetAabb.toSharedAabb(), otherAabb.toSharedAabb())) {
                return true
            }
        }
        return false
    }

    private fun computeWorldAabb(node: TransformableNode, shape: Box): Aabb? {
        val worldScale = node.worldScale
        val localHalfExtents = Vector3(
            shape.size.x * worldScale.x * 0.5f,
            shape.size.y * worldScale.y * 0.5f,
            shape.size.z * worldScale.z * 0.5f,
        )
        val localCenter = Vector3(
            shape.center.x * worldScale.x,
            shape.center.y * worldScale.y,
            shape.center.z * worldScale.z,
        )
        val rotatedCenter = Quaternion.rotateVector(node.worldRotation, localCenter)
        val worldCenter = Vector3.add(node.worldPosition, rotatedCenter)
        val q = node.worldRotation
        val xx = q.x * q.x
        val yy = q.y * q.y
        val zz = q.z * q.z
        val xy = q.x * q.y
        val xz = q.x * q.z
        val yz = q.y * q.z
        val wx = q.w * q.x
        val wy = q.w * q.y
        val wz = q.w * q.z

        val m00 = 1f - 2f * (yy + zz)
        val m01 = 2f * (xy - wz)
        val m02 = 2f * (xz + wy)

        val m10 = 2f * (xy + wz)
        val m11 = 1f - 2f * (xx + zz)
        val m12 = 2f * (yz - wx)

        val m20 = 2f * (xz - wy)
        val m21 = 2f * (yz + wx)
        val m22 = 1f - 2f * (xx + yy)

        val halfExtents = Vector3(
            abs(m00) * localHalfExtents.x + abs(m01) * localHalfExtents.y + abs(m02) * localHalfExtents.z,
            abs(m10) * localHalfExtents.x + abs(m11) * localHalfExtents.y + abs(m12) * localHalfExtents.z,
            abs(m20) * localHalfExtents.x + abs(m21) * localHalfExtents.y + abs(m22) * localHalfExtents.z,
        )

        return Aabb(center = worldCenter, halfExtents = halfExtents)
    }

    private fun clearAllNodes() {
        placedNodes.forEach { node ->
            val parent = node.parent as? AnchorNode
            node.setParent(null)
            parent?.anchor?.detach()
            parent?.setParent(null)
        }
        placedNodes.clear()
        controller.reportPlacedCount(0)
        controller.reportError(null)
    }

    private fun removeNode(node: TransformableNode) {
        val parent = node.parent as? AnchorNode
        node.setParent(null)
        parent?.anchor?.detach()
        parent?.setParent(null)
    }

    private fun createClampedAnchor(hit: HitResult): Anchor {
        val frame = arSceneView.arFrame ?: return hit.createAnchor()
        val session = arSceneView.session ?: return hit.createAnchor()

        val cameraPose = frame.camera.pose
        val hitPose = hit.hitPose

        val camX = cameraPose.tx()
        val camY = cameraPose.ty()
        val camZ = cameraPose.tz()

        val hitX = hitPose.tx()
        val hitY = hitPose.ty()
        val hitZ = hitPose.tz()

        val dx = hitX - camX
        val dy = hitY - camY
        val dz = hitZ - camZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)

        if (distance <= EPSILON) {
            return hit.createAnchor()
        }

        val clampedDistance = params.distancePolicy.clamp(distance)
        if (abs(clampedDistance - distance) <= 1e-3f) {
            return hit.createAnchor()
        }

        val ratio = clampedDistance / distance
        val clampedPose = Pose.makeTranslation(
            camX + dx * ratio,
            camY + dy * ratio,
            camZ + dz * ratio,
        )
        return session.createAnchor(clampedPose)
    }

    private fun reloadModel() {
        clearAllNodes()
        modelRenderable = null
        loadModel(force = true)
    }

    private fun loadModel(force: Boolean) {
        if (!force && modelRenderable != null) return
        if (!validateModelDimensions()) return
        controller.reportModelLoading(true)
        val file = File(params.filePath)
        if (!file.exists()) {
            controller.reportModelLoading(false)
            controller.reportError(ArSceneController.SceneError.ModelLoadingFailed)
            return
        }
        val uri = Uri.fromFile(file)
        ModelRenderable.builder()
            .setSource(requireContext(), uri)
            .setRegistryId(uri.toString())
            .build()
            .thenAccept { renderable: ModelRenderable ->
                renderable.isShadowCaster = true
                renderable.isShadowReceiver = true
                modelRenderable = renderable
                updateModelScale(renderable)
                controller.reportModelLoading(false)
                controller.reportError(null)
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Failed to load renderable from ${file.absolutePath}", throwable)
                controller.reportModelLoading(false)
                controller.reportError(ArSceneController.SceneError.ModelLoadingFailed)
                null
            }
    }

    private fun updateModelScale(renderable: ModelRenderable) {
        val shape = renderable.collisionShape as? Box ?: run {
            modelScale = Vector3.one()
            return
        }

        val targetSize = ArScaleCalculator.toMeters(modelDimensionsMm())
        val sourceSize = ArModelDimensionsMeters(
            width = shape.size.x,
            height = shape.size.y,
            depth = shape.size.z,
        )
        val uniformScale = ArScaleCalculator.computeUniformScale(
            source = sourceSize,
            target = targetSize,
            policy = params.scalePolicy,
        )
        modelScale = Vector3(uniformScale, uniformScale, uniformScale)
    }

    private fun planeMatchesPlacement(plane: Plane): Boolean {
        return ArPlacementPolicy.supportsPlane(
            placement = params.placementPolicy,
            planeType = plane.toArPlaneType(),
        )
    }

    private fun validateModelDimensions(): Boolean {
        return when (ArDimensionValidator.validate(modelDimensionsMm())) {
            ArValidationResult.Valid -> true
            is ArValidationResult.Invalid -> {
                controller.reportModelLoading(false)
                controller.reportError(ArSceneController.SceneError.InvalidDimensions)
                false
            }
        }
    }

    private fun modelDimensionsMm(): ArModelDimensionsMm {
        return ArModelDimensionsMm(
            widthMm = params.widthMm,
            heightMm = params.heightMm,
            depthMm = params.depthMm,
        )
    }

    private fun Plane.toArPlaneType(): ArPlaneType {
        return when (type) {
            Plane.Type.HORIZONTAL_UPWARD_FACING -> ArPlaneType.HorizontalUpward
            Plane.Type.HORIZONTAL_DOWNWARD_FACING -> ArPlaneType.HorizontalDownward
            Plane.Type.VERTICAL -> ArPlaneType.Vertical
        }
    }

    private fun anchorPoseToString(anchor: Anchor): String =
        anchor.pose.translation.contentToString()

    private data class Aabb(
        val center: Vector3,
        val halfExtents: Vector3,
    ) {
        fun toSharedAabb(): com.poroshin.rut.ar.common.ar.domain.ArAabb {
            return com.poroshin.rut.ar.common.ar.domain.ArAabb(
                center = com.poroshin.rut.ar.common.ar.domain.ArVector3(
                    x = center.x,
                    y = center.y,
                    z = center.z,
                ),
                halfExtents = com.poroshin.rut.ar.common.ar.domain.ArVector3(
                    x = halfExtents.x,
                    y = halfExtents.y,
                    z = halfExtents.z,
                ),
            )
        }
    }

    companion object {
        private const val TAG = "CustomArFragment"
        private const val EPSILON = 1e-5f

        fun newInstance(params: ArObjectParams): CustomArFragment {
            return CustomArFragment().apply {
                arguments = params.toBundle()
            }
        }
    }
}
