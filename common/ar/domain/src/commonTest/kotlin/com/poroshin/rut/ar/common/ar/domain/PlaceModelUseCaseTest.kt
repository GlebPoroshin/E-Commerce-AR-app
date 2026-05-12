package com.poroshin.rut.ar.common.ar.domain

import com.poroshin.rut.ar.common.ar.domain.model.ArPose
import com.poroshin.rut.ar.common.ar.domain.result.PlacementResult
import com.poroshin.rut.ar.common.ar.domain.usecase.PlaceModelUseCase
import com.poroshin.rut.ar.common.pdp.domain.ArPlacement
import kotlin.test.Test
import kotlin.test.assertIs

class PlaceModelUseCaseTest {

    private val useCase = PlaceModelUseCase()

    private fun pose(x: Float = 0f, y: Float = 0f, z: Float = 0f) = ArPose(
        translation = floatArrayOf(x, y, z),
        rotation = floatArrayOf(0f, 0f, 0f, 1f),
    )

    private fun extents(x: Float = 0.1f, y: Float = 0.1f, z: Float = 0.1f) =
        ArVector3(x, y, z)

    @Test
    fun allowsPlacementOnMatchingPlane() {
        val result = useCase(
            pose = pose(),
            halfExtents = extents(),
            alreadyPlaced = emptyList(),
            placement = ArPlacement.FLOOR,
            planeType = ArPlaneType.HorizontalUpward,
            isSingleMode = false,
        )

        assertIs<PlacementResult.Updated>(result)
    }

    @Test
    fun rejectsPlacementOnNonMatchingPlane() {
        val result = useCase(
            pose = pose(),
            halfExtents = extents(),
            alreadyPlaced = emptyList(),
            placement = ArPlacement.FLOOR,
            planeType = ArPlaneType.Vertical,
            isSingleMode = false,
        )

        assertIs<PlacementResult.Rejected>(result)
    }

    @Test
    fun rejectsCollision() {
        val existing = pose(0f, 0f, 0f) to extents(0.5f, 0.5f, 0.5f)
        val newPose = pose(0.1f, 0f, 0f)

        val result = useCase(
            pose = newPose,
            halfExtents = extents(0.5f, 0.5f, 0.5f),
            alreadyPlaced = listOf(existing),
            placement = ArPlacement.FLOOR,
            planeType = ArPlaneType.HorizontalUpward,
            isSingleMode = false,
        )

        assertIs<PlacementResult.Rejected>(result)
    }

    @Test
    fun allowsNonCollidingPlacement() {
        val existing = pose(0f, 0f, 0f) to extents(0.1f, 0.1f, 0.1f)
        val newPose = pose(5f, 0f, 0f)

        val result = useCase(
            pose = newPose,
            halfExtents = extents(0.1f, 0.1f, 0.1f),
            alreadyPlaced = listOf(existing),
            placement = ArPlacement.FLOOR,
            planeType = ArPlaneType.HorizontalUpward,
            isSingleMode = false,
        )

        assertIs<PlacementResult.Updated>(result)
    }

    @Test
    fun rejectsSingleModeWhenSceneNotEmpty() {
        val existing = pose(10f, 0f, 0f) to extents(0.1f, 0.1f, 0.1f)

        val result = useCase(
            pose = pose(),
            halfExtents = extents(),
            alreadyPlaced = listOf(existing),
            placement = ArPlacement.FLOOR,
            planeType = ArPlaneType.HorizontalUpward,
            isSingleMode = true,
        )

        assertIs<PlacementResult.Rejected>(result)
    }

    @Test
    fun allowsSingleModeWhenSceneEmpty() {
        val result = useCase(
            pose = pose(),
            halfExtents = extents(),
            alreadyPlaced = emptyList(),
            placement = ArPlacement.FLOOR,
            planeType = ArPlaneType.HorizontalUpward,
            isSingleMode = true,
        )

        assertIs<PlacementResult.Updated>(result)
    }
}
