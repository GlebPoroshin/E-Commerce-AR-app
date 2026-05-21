# E-Commerce AR App

Кроссплатформное приложение электронной коммерции с технологией дополненной реальности (AR) для размещения 3D-моделей товаров в реальном пространстве.

> **Дипломный проект (ВКР)** — исследование интеграции Kotlin Multiplatform с платформо-специфичным AR-рендерингом в мобильных приложениях.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![KMP](https://img.shields.io/badge/KMP-Multiplatform-4285F4?style=for-the-badge)](https://kotlinlang.org/docs/multiplatform-mobile-get-started.html)
[![Android](https://img.shields.io/badge/Android-28+-3DDC84?style=for-the-badge&logo=android)](https://www.android.com/)
[![iOS](https://img.shields.io/badge/iOS-16+-000?style=for-the-badge&logo=apple)](https://www.apple.com/ios)
[![Gradle](https://img.shields.io/badge/Gradle-8.7.3-02303A?style=for-the-badge&logo=gradle)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

## Демонстрация

### Основные экраны

| Каталог товаров (PLP) | Карточка товара (PDP) | AR размещение |
|:---:|:---:|:---:|
| ![PLP](docs/screenshots/plp.png) | ![PDP](docs/screenshots/pdp.png) | ![AR](docs/screenshots/ar.png) |
| Android (слева) · iOS (справа) | Скачать модель → Посмотреть в AR | ARCore (Android) · RealityKit (iOS) |

### Корзина и обработка ошибок

| Корзина | Ошибка сети (PLP) | Ошибка загрузки 3D-модели |
|:---:|:---:|:---:|
| ![Cart](docs/screenshots/cart.png) | ![Error PLP](docs/screenshots/error_plp.png) | ![Error AR](docs/screenshots/error_ar.png) |
| Счётчик на иконке, итого | Кнопка «Повторить» | Снекбар + диалог ошибки |

## Ключевые функции

### 📱 PLP (Product List Page)
- **Каталог товаров** с shimmer-эффектом загрузки
- **Пагинация** для оптимальной загрузки данных
- **Адаптивный дизайн** на Jetpack Compose (Android) / SwiftUI (iOS)

### 🛍️ PDP (Product Detail Page)
- **Детальная информация** о товаре (название, описание, характеристики)
- **Галерея изображений** с поддержкой SVG (Android) и HD (iOS)
- **Ценообразование** с отображением скидок и финальной цены
- **Кнопка добавления в корзину** с интеграцией локального хранилища

### 🔮 AR Placement (Дополненная реальность)
- **Android**: ARCore 1.48.0 с использованием Sceneform 1.23.0
- **iOS**: RealityKit + ARKit (iOS 16+)
- **Поддержка поверхностей**: горизонтальные (полы) и вертикальные (стены)
- **Мультиобъектный режим**: размещение нескольких товаров одновременно
- **Жесты управления**:
  - 🖐️ Перемещение (drag)
  - ↻ Вращение (pinch rotate)
  - ↕️ Масштаб (pinch zoom)
- **Работает только на физических устройствах** (не поддерживается в эмуляторе)

#### Жизненный цикл 3D-модели на клиенте

![Жизненный цикл 3D-модели](docs/diagrams/model_lifecycle.svg)

### 🛒 Cart (Корзина)
- **Локальное хранилище** на SQLDelight 2.1.0
- **Синхронизация** между PDP и корзиной
- **Счётчик на иконке** с обновлением в реальном времени

## Архитектура

### Структура модулей

![Архитектура мобильного приложения](docs/diagrams/mobile_packages.svg)

### Clean Architecture

Проект строго следует принципам Clean Architecture с разделением на три слоя:

```
common/
├── core/                    # Конфигурация, утилиты, модели ошибок
│   ├── BackendConfig        # Настройка Tailscale Funnel URL
│   ├── NetworkError         # Доменная модель ошибок сети
│   └── DeviceIdProvider     # Провайдер уникального ID устройства
│
├── mvi/                     # Базовые классы MVI паттерна
│   └── SharedViewModel      # Базовый класс с StateFlow/SharedFlow
│
├── plp/                     # Feature: Product List Page
│   ├── domain/              # Бизнес-логика и интерфейсы
│   │   ├── models/Product   # Доменные модели товаров
│   │   └── GetPlpProductsUseCase (interface)
│   ├── data/                # Реализация, DataSource, сетевые модели
│   │   ├── PlpDataSource    # HTTP клиент через Ktor
│   │   └── GetPlpProductsUseCaseImpl
│   └── presentation/        # UI-слой, State/Event/Action
│       ├── PlpViewModel
│       ├── PlpState (sealed class)
│       ├── PlpEvent (sealed class)
│       └── PlpAction (sealed class)
│
├── pdp/                     # Feature: Product Detail Page
│   ├── domain/              # Интерфейсы и модели
│   │   └── GetProductPageInfoUseCase (interface)
│   ├── data/                # Реализация
│   │   ├── PdpDataSource    # Ktor с DeviceID plugin
│   │   └── GetProductPageInfoUseCaseImpl
│   └── presentation/        # ViewModel/State/Event/Action
│       └── PdpViewModel
│
├── ar/                      # Feature: Augmented Reality
│   ├── domain/              # AR модели и типы размещения
│   │   ├── ArModel          # 3D-модель товара
│   │   └── PlacementType    # Горизонтальное/вертикальное размещение
│   ├── data/                # AR DataSource и логика загрузки
│   │   └── ArDataSource     # Получение GLTF моделей
│   └── presentation/        # Платформо-специфичная визуализация
│       ├── ArViewModel
│       ├── ArFragment       # Android Compose Fragment
│       └── ArScreen         # iOS SwiftUI Screen
│
├── cart/                    # Feature: Shopping Cart
│   ├── domain/              # Cart models, UseCases
│   │   ├── CartItem
│   │   └── GetCartUseCase (interface)
│   ├── data/                # SQLDelight persistence
│   │   ├── CartDatabase
│   │   └── CartRepositoryImpl (миграция legacy)
│   └── presentation/        # CartQuantityViewModel
│       └── CartQuantityViewModel
│
└── umbrella/                # DI Assembly (Koin)
    └── KoinModules          # Полная инъекция зависимостей
```

### MVI ViewModel Pattern

```kotlin
class PlpViewModel(
    private val getPlpProductsUseCase: GetPlpProductsUseCase,
) : SharedViewModel<PlpState, PlpEvent, PlpAction>(
    initialState = PlpState.Loading
) {
    override suspend fun handleEvent(event: PlpEvent) = when (event) {
        is PlpEvent.LoadProducts -> loadProducts(event.page)
        is PlpEvent.RetryLoad -> loadProducts(0)
    }
    
    private suspend fun loadProducts(page: Int) {
        // Обновляет viewState: StateFlow<PlpState>
        updateState { copy(isLoading = true) }
        // Отправляет одноразовые side-effects через viewAction: SharedFlow<PlpAction>
        sendAction(PlpAction.ScrollToTop)
    }
}
```

**Ключевые отличия от стандартного MVVM:**
- `viewState: StateFlow<S>` вместо `state` (конвенция проекта)
- `viewAction: SharedFlow<A>` вместо `actions` (одноразовые события)
- `onEvent(event: E)` публичный, `handleEvent(event: E)` абстрактный
- Никаких других публичных методов

## Технологический стек

### Shared Логика (KMP)

| Компонент | Версия | Назначение |
|-----------|--------|-----------|
| **Kotlin** | 2.1.21 | Язык программирования |
| **Ktor Client** | 3.2.3 | HTTP-клиент с custom DeviceID plugin |
| **Koin** | 4.1.0 | Dependency Injection (Kotlin-first) |
| **SQLDelight** | 2.1.0 | Типизированное хранилище данных |
| **KotlinX Serialization** | 1.8.0 | JSON сериализация |
| **MultiplatformSettings** | 1.3.0 | Key-value хранилище |

### Android

| Компонент | Версия | Назначение |
|-----------|--------|-----------|
| **Jetpack Compose** | Latest | Декларативный UI |
| **Compose Material3** | Latest | Material Design 3 компоненты |
| **ARCore** | 1.48.0 | AR функциональность |
| **Sceneform** | 1.23.0 | 3D-рендеринг, GLTF загрузка |
| **Coil3** | 3.3.0 | Загрузка изображений (с SVG support) |
| **Cicerone** | Latest | Навигация между экранами |
| **Android Fragment** | Latest | Контейнер для ARFragment |
| **EncryptedSharedPreferences** | Latest | Зашифрованное хранилище |

### iOS

| Компонент | Версия | Назначение |
|-----------|--------|-----------|
| **SwiftUI** | iOS 16+ | Декларативный UI |
| **RealityKit** | iOS 16+ | 3D-рендеринг GLTF моделей |
| **ARKit** | iOS 16+ | AR функциональность |
| **URLSession** | iOS 16+ | HTTP запросы через Ktor |
| **Keychain** | iOS 16+ | Зашифрованное хранилище |

### Backend Integration

- **Tailscale Funnel**: безопасное туннелирование HTTPS на локальный Spring Boot сервер
- **DeviceID Plugin**: автоматическая передача ID устройства в заголовке каждого запроса
- **Реализация**: `AREcommerceApi` (отдельный репозиторий)

## Требования к сборке

### Android

- **Android Studio** Ladybug или новее
- **Android SDK** 28+ (minSdk в build.gradle.kts = 28)
- **Gradle** 8.7.3
- **Kotlin** 2.1.21
- **Физическое устройство** с поддержкой ARCore (AR-функции не работают в эмуляторе)
  - Проверка: Settings → Google → ARCore → установлена актуальная версия
- **JDK** 11+

### iOS

- **Xcode** 15.0 или новее
- **iOS Deployment Target**: 16.0+
- **CocoaPods** (автоматически через KMP)
- **Физическое устройство** iPhone/iPad (AR требует физического устройства)
  - Эмулятор не поддерживает RealityKit + ARKit
- **Apple Developer Account** (для сборки и запуска на устройстве)

### Разработка (Общее)

- **macOS** 12+
- **Kotlin 2.1.21** (через gradle wrapper)
- **Git** 2.30+
- **Tailscale CLI** (для локального тестирования backend)

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/poroshin/AREcommerceApp.git
cd E-Commerce-AR-app
```

### 2. Синхронизация Gradle

```bash
./gradlew sync
# или в Android Studio: File → Sync Now
```

### 3. Конфигурация Backend

Приложение подключается к backend через **Tailscale Funnel**.

#### Шаг 1: Установка Tailscale

```bash
brew install tailscale
sudo tailscale up
# Откроется браузер для авторизации
```

#### Шаг 2: Активация Funnel

```bash
# В админпанели https://login.tailscale.com/admin
# 1. Включить MagicDNS и HTTPS Certificates
# 2. Добавить в ACL:
{
  "nodeAttrs": [
    {"target": ["autogroup:member"], "attr": ["funnel"]}
  ]
}
```

#### Шаг 3: Запуск Funnel

```bash
# На машине с backend сервером
sudo tailscale funnel 8080
# Выведет URL вида: https://macbook.tail-xxxxx.ts.net
```

#### Шаг 4: Конфигурирование приложения

Отредактировать `common/core/src/commonMain/kotlin/com/poroshin/rut/ar/BackendConfig.kt`:

```kotlin
object BackendConfig {
    const val BASE_URL = "https://macbook.tail-xxxxx.ts.net" // Ваш Tailscale URL
    const val TIMEOUT_MS = 15000L
}
```

### 4. Запуск на Android

```bash
# Убедитесь, что устройство подключено
./gradlew installDebug

# Или из Android Studio:
# Build → Build Variants → выбрать debug → Run
```

**Проверка ARCore:**
```bash
adb shell pm list packages | grep arcore
```

### 5. Запуск на iOS

```bash
# Генерация Xcode проекта (CocoaPods)
./gradlew iosApp:downloadDependencies

# Откройте iosApp/iosApp.xcworkspace в Xcode
open iosApp/iosApp.xcworkspace

# Выберите physical device и нажмите Run (Cmd+R)
```

**Убедитесь:**
- ✅ Device физический (не simulator для AR)
- ✅ Team ID установлен в Xcode
- ✅ Provisioning Profile актуален

## Структура проекта

```
E-Commerce-AR-app/
├── common/                      # KMP shared module
│   ├── core/                    # Конфигурация, утилиты
│   ├── mvi/                     # Базовый SharedViewModel
│   ├── plp/                     # Feature: PLP
│   │   ├── domain/
│   │   ├── data/
│   │   └── presentation/
│   ├── pdp/                     # Feature: PDP
│   ├── ar/                      # Feature: AR
│   ├── cart/                    # Feature: Cart
│   └── umbrella/                # DI Koin assembly
│
├── androidApp/                  # Android-специфичный код
│   ├── src/main/kotlin/         # Application, MainActivity
│   ├── src/main/res/            # Resources, strings.xml
│   └── build.gradle.kts
│
├── iosApp/                      # iOS-специфичный код
│   ├── iosApp/                  # SwiftUI App, Router
│   ├── iosApp.xcworkspace/      # Generated by CocoaPods
│   └── Podfile
│
├── gradle/                      # Gradle wrapper
├── build.gradle.kts             # Root build script
├── settings.gradle.kts          # Module configuration
├── gradle.properties            # Gradle properties
└── README.md                    # Этот файл
```

## Примеры использования

### Загрузка каталога товаров

```kotlin
// ViewModel обрабатывает событие
plpViewModel.onEvent(PlpEvent.LoadProducts(page = 0))

// Слушаем state через UI
plpViewModel.viewState.collect { state ->
    when (state) {
        PlpState.Loading -> showShimmer()
        is PlpState.Content -> renderProducts(state.products)
        is PlpState.Error -> showError(state.message)
    }
}

// Side-effects (scroll to top)
plpViewModel.viewAction.collect { action ->
    when (action) {
        PlpAction.ScrollToTop -> scrollToPosition(0)
    }
}
```

### Размещение товара в AR

```kotlin
// 1. На PDP нажимаем "Просмотреть в AR"
pdpViewModel.onEvent(PdpEvent.OpenArView)

// 2. ArViewModel загружает 3D-модель через useCase
arViewModel.onEvent(ArEvent.LoadModel(productId = "123"))

// 3. На Android: Sceneform рендерит GLTF в ARCore
// На iOS: RealityKit загружает ModelEntity из GLTF

// 4. Жесты управления привязаны к Scene
scene.onTap { hitResult ->
    // Размещение модели в точке касания
}
```

### Добавление в корзину

```kotlin
// PDP ViewModel
pdpViewModel.onEvent(PdpEvent.AddToCart(quantity = 2))

// Cart UseCase сохраняет в SQLDelight
// CartQuantityViewModel обновляет счётчик иконки корзины
cartQuantityViewModel.viewState.collect { count ->
    updateCartBadge(count) // "+2"
}
```

## Тестирование

### Unit-тесты (обязательные)

Каждый UseCase покрыт Unit-тестами на MockK:

```bash
./gradlew :common:plp:domain:test
./gradlew :common:pdp:domain:test
./gradlew :common:cart:domain:test
./gradlew :common:ar:domain:test
```

**AAA паттерн** (Arrange-Act-Assert):
```kotlin
@Test
fun `LoadProducts returns products list`() {
    // Arrange
    val mockRepository = mockk<ProductRepository>()
    coEvery { mockRepository.getProducts(0) } returns listOf(product1, product2)
    val useCase = GetPlpProductsUseCaseImpl(mockRepository)
    
    // Act
    val result = useCase(GetPlpProductsUseCase.Params(page = 0))
    
    // Assert
    assertEquals(2, result.size)
    coVerify { mockRepository.getProducts(0) }
}
```

### UI-тесты (по необходимости)

```bash
# Android Compose UI tests
./gradlew :androidApp:connectedAndroidTest

# iOS XCTest (вручную в Xcode)
```

### Linting & Code Style

Проект использует **detekt** и **ktlint**:

```bash
# Проверка кода перед коммитом
./gradlew detekt
./gradlew ktlintCheck

# Автоматическое исправление
./gradlew ktlintFormat
```

## Миграции базы данных

Cart использует SQLDelight с миграциями. При обновлении схемы:

1. Отредактируйте `.sqldelight` файл в `common/cart/data/src/commonMain/sqldelight/`
2. Создайте новый файл миграции `v2.sql`, `v3.sql` и т.д.
3. Перестройте проект: `./gradlew build`

SQLDelight автоматически применит миграции при первом открытии DB.

## Документация API

### Backend (Tailscale)

Документация backend API доступна в репозитории [`AREcommerceApi`](https://github.com/poroshin/AREcommerceApi):

- **GET** `/api/v1/products` — каталог товаров (PLP)
- **GET** `/api/v1/products/{id}` — информация о товаре (PDP)
- **GET** `/api/v1/products/{id}/ar-model` — 3D-модель (GLTF)

Все запросы требуют заголовок:
```
X-Device-ID: <uuid>
```

## Производительность

### Оптимизация загрузки

- ✅ **Shimmer** на PLP: параллельная загрузка контента
- ✅ **Пагинация**: ~20 товаров на странице
- ✅ **Image caching** (Coil3): в памяти + диск
- ✅ **Ленивая загрузка** 3D-моделей: только при открытии AR
- ✅ **Proguard/R8** (Android release): минификация кода

### Размер приложения

- **Android APK**: ~45 MB (release с R8)
- **iOS IPA**: ~60 MB (с RealityKit USDZ кешем)

## Структура коммитов

Коммиты следуют формату (задача отслеживается через git):

```
[FEATURE-123] Add AR gesture controls for model rotation
```

## Лицензия

MIT License — см. [LICENSE](LICENSE)

## Связанные проекты

### Backend API

**[AREcommerceApi](https://github.com/poroshin/AREcommerceApi)** — Spring Boot backend с REST API, БД товаров, 3D-моделей и интеграцией с Tailscale Funnel.

**Требует:**
- Java 17+
- PostgreSQL 14+
- Docker (опционально для локальной разработки)

**Запуск:**
```bash
cd AREcommerceApi
./mvnw spring-boot:run
# Backend слушает localhost:8080
# Tailscale Funnel выставляет наружу через https://macbook.tail-xxxxx.ts.net
```

## Контрибьютинг

1. Fork репозиторий
2. Создайте feature ветку: `git checkout -b TASK-123-description`
3. Коммитьте изменения: `git commit -m "[TASK-123] Description"`
4. Пушьте в fork: `git push origin TASK-123-description`
5. Откройте Pull Request с описанием изменений

Перед PR убедитесь:
- ✅ Все тесты проходят: `./gradlew test`
- ✅ Code style прошёл: `./gradlew detekt ktlintCheck`
- ✅ Нет unused imports
- ✅ Коммиты в формате `[TASK-XXX] Message`

## Troubleshooting

### Android: ARCore не инициализируется

```
E/ArCoreException: UNAVAILABLE
```

**Решение:**
```bash
# Обновить Google Play Services for AR
adb shell am start -a android.intent.action.VIEW \
  -d "market://details?id=com.google.ar.core"

# Проверить версию
adb shell pm list packages | grep arcore
```

### iOS: RealityKit не загружает модель

```
error: Model loading failed
```

**Проверить:**
1. URL модели доступна (HTTPS через Tailscale)
2. Формат GLTF (не usdz)
3. Размер модели < 50 MB
4. Физическое устройство (не simulator)

### Gradle: `kotlin.jvm.target` конфликт

```
error: expected: '1.8', found: '11'
```

**Решение:**
```bash
./gradlew clean
./gradlew wrapper --gradle-version 8.7.3
```

### Tailscale Funnel: 403 Forbidden

**Причины:**
- ACL не включены в админпанели
- Funnel не запущен на backend машине
- Домен истёк (переподключитесь)

```bash
sudo tailscale logout
sudo tailscale up
sudo tailscale funnel 8080
```

## Авторы

**Глеб Порошин**  
Дипломный проект (ВКР), 2025  
[Email](mailto:Gleb.Poroshin@lemanapro.kz)

## Благодарности

- Спасибо команде Kotlin за Multiplatform
- ARCore/RealityKit документации за примеры
- Open source сообществу за Ktor, Koin, SQLDelight, Coil

---

**Последнее обновление:** Май 2025  
**Статус:** Активное развитие (дипломный проект)
