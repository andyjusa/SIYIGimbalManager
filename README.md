# SIYIGimbalManager

## How To Use Library

build.gradle - project
```gradle:build.gradle
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
}
```


build.gradle - module
```gradle:build.gradle
	dependencies {
	        implementation 'com.github.andyjusa:SIYIGimbalManager:v0.1'
	}
```
## Usage

- init
-	```java:example0.java
	  mControlManager = new ControlManager();
	```


- sendMessage
- 	```java:example0.java
	  Commands cmd = new Commands();  
	  mControlManager.sendData(cmd.getFirmwareVersion(), new NullArgs());
	```


- setListener
  ```java:example0.java
    mControlManager.setOnListener(new RecvListener{
	    ...
	    }
    );
	```
