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

