# HomeBrewHelper 🍺

A comprehensive homebrewing application supporting beer, wine, mead, cider, kombucha, and specialty fermented beverages. Built with modern Android development practices using Jetpack Compose and Kotlin.

## Features

### ✅ Recipe Management System (Phase 1 - Complete)
- **Multi-Beverage Support**: Create recipes for Beer, Wine, Mead, Cider, Kombucha, and Specialty brews
- **Recipe Builder**: Intuitive step-by-step recipe creation with comprehensive forms
- **Recipe Library**: Browse, search, and filter recipes by beverage type, difficulty, and favorites
- **Recipe Scaling**: Automatically scale recipes to different batch sizes with ingredient proportions
- **Recipe Variations**: Create and track recipe variations with version control
- **Cost Tracking**: Calculate estimated and actual recipe costs

### 📱 Modern User Interface
- **Material Design 3**: Clean, accessible interface following Google's design guidelines
- **Jetpack Compose**: Modern declarative UI with smooth animations and interactions
- **Responsive Design**: Optimized for phones and tablets
- **Dark Mode Ready**: Supports system dark/light theme preferences

### 🏗️ Production-Ready Architecture
- **MVVM + Repository Pattern**: Clean architecture with separation of concerns
- **Hilt Dependency Injection**: Fully configured dependency injection
- **Room Database**: Type-safe local database with comprehensive relationships
- **Reactive UI**: StateFlow and Compose integration for real-time updates

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material3
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room with SQLite
- **Dependency Injection**: Hilt
- **Navigation**: Compose Navigation
- **Async Programming**: Kotlin Coroutines and Flow
- **Date/Time**: kotlinx-datetime
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36

## Getting Started

### Prerequisites
- Android Studio Ladybug or later
- JDK 11+
- Android SDK 36

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/cazziuz-realone/homebrewhelper.git
   ```

2. Open the project in Android Studio

3. Sync Gradle dependencies

4. Run the app on a device or emulator (API 24+)

### Default Data
The app automatically initializes with a comprehensive set of default ingredients covering all supported beverage types when first launched.

## Project Structure

```
app/src/main/java/com/example/homebrewhelper/
├── MainActivity.kt                     # Main activity with Compose setup
├── HomeBrewHelperApplication.kt        # Hilt application class
├── data/
│   ├── database/                       # Room database configuration
│   │   ├── HomeBrewDatabase.kt         # Main database class
│   │   ├── converter/                  # Type converters
│   │   ├── dao/                        # Data access objects
│   │   └── entity/                     # Database entities
│   ├── repository/                     # Repository layer
│   └── model/                          # Data models and enums
├── ui/
│   ├── screens/                        # Compose screens
│   ├── components/                     # Reusable UI components
│   ├── navigation/                     # Navigation setup
│   └── theme/                          # Material3 theme
└── viewmodel/                          # ViewModels for state management
```

## Database Schema

### Core Entities
- **Recipe**: Complete recipe information with metadata
- **Ingredient**: Comprehensive ingredient database with characteristics
- **RecipeIngredient**: Junction table linking recipes to ingredients with usage details

### Supported Beverage Types
- **Beer**: Grain bills, hop schedules, yeast strains
- **Wine**: Grape varieties, acid levels, fermentation notes
- **Mead**: Honey types, nutrient schedules, aging requirements
- **Cider**: Apple varieties, acid balance, carbonation methods
- **Kombucha**: Tea types, SCOBY management, flavoring schedules
- **Specialty**: Custom and experimental fermented beverages

## Development Roadmap

### Phase 2 (Enhanced Features) - Planned
- [ ] **Batch Tracking**: Monitor active fermentations
- [ ] **Equipment Management**: Track brewing equipment and maintenance
- [ ] **Inventory Management**: Ingredient stock tracking with alerts
- [ ] **Quality Control**: Testing protocols and record keeping
- [ ] **Water Chemistry**: pH and mineral adjustment calculations

### Phase 3 (Advanced Features) - Future
- [ ] **Advanced Analytics**: Detailed brewing statistics and trends
- [ ] **IoT Integration**: Sensor data integration for temperature and gravity
- [ ] **Community Features**: Recipe sharing and brewing community
- [ ] **Commercial Compliance**: Support for commercial brewing regulations

## Architecture Highlights

### Clean Architecture
- **Separation of Concerns**: Clear boundaries between UI, business logic, and data
- **Dependency Inversion**: High-level modules don't depend on low-level modules
- **Testability**: Repository pattern enables easy unit testing with mocks

### Reactive Programming
- **StateFlow**: Reactive state management with lifecycle awareness
- **Flow**: Asynchronous data streams for database operations
- **Compose Integration**: Seamless reactive UI updates

### Error Handling
- **Result Types**: Consistent error handling with Kotlin Result
- **User Feedback**: Comprehensive error states in UI
- **Graceful Degradation**: App continues functioning even with partial failures

## Contributing

### Code Standards
- Follow Kotlin coding conventions
- Use Jetpack Compose best practices (stateless composables, state hoisting)
- Write unit tests for ViewModels and repositories
- Maintain comprehensive documentation

### Development Setup
1. Ensure all tests pass before submitting PRs
2. Follow the existing architecture patterns
3. Update documentation for new features
4. Use meaningful commit messages

## Testing

The project includes comprehensive testing infrastructure:
- **Unit Tests**: ViewModel and repository testing
- **Integration Tests**: Database operations
- **UI Tests**: Critical user flows with Compose testing

Run tests with:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Performance

### Optimizations
- Lazy loading with pagination for large datasets
- Efficient database queries with proper indexing
- Stateless Compose components for minimal recomposition
- Background processing for data initialization

### Monitoring
- Memory leak prevention with proper lifecycle management
- Database query optimization
- UI performance monitoring with Compose tools

## Security & Privacy

- **Local Data**: All data stored locally on device
- **No Network**: Currently offline-first (cloud sync planned for future)
- **Data Integrity**: Soft deletes and audit trails
- **User Control**: Full control over personal brewing data

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Architecture Inspiration**: Based on comprehensive HomeBrewHelper Core Components Architecture
- **Design**: Material Design 3 guidelines and Android best practices
- **Community**: Built for the passionate homebrewing community

## Support

For questions, issues, or feature requests:
- Create an issue on GitHub
- Check the documentation in the `/docs` folder
- Review the comprehensive code comments throughout the project

---

**HomeBrewHelper** - Crafting better brews through better software 🍻