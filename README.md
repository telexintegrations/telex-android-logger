## Telex Error Handler
An Android library for handling and logging errors to Telex channels.
### Installation

- Add JitPack repository to your build file:
``` bash
repositories {
    maven { url 'https://jitpack.io' }
}
```
- Add the dependency:
  ``` bash
  dependencies {
    implementation 'https://github.com/telexintegrations/telex-android-logger'
}
```
- Initialize Yor application class
``` bash
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ExceptionLogger.initialize(
            apiUrl = "your_telex_webhook_url",
            username = "default_user"
        )
    }
}
```

- Log execptions
``` bash
// Global exception handling is enabled by default

// Manual exception logging
try {
    // Your code
} catch (e: Exception) {
    ExceptionLogger.logException(e)
}

// Logging with context
try {
    // Your code
} catch (e: Exception) {
    ExceptionLogger.logException(
        exception = e,
        source = "PaymentProcessor",
        method = "processPayment",
        additionalInfo = mapOf(
            "userId" to "123",
            "amount" to "100.00"
        )
    )
}
```

- Logging with additional context
``` bash
class PaymentProcessor {
    fun processPayment(amount: Double, userId: String) {
        try {
            // Payment processing code
        } catch (e: Exception) {
            ExceptionLogger.logException(
                exception = e,
                source = "PaymentProcessor",
                method = "processPayment",
                additionalInfo = mapOf(
                    "amount" to amount,
                    "userId" to userId,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }
}
```

  
