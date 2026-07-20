plugins {
	id("chat.spring-boot-app")
}

group = "com.chat"
version = "0.0.1-SNAPSHOT"

dependencies {
	implementation(projects.user)
	implementation(projects.chat)
	implementation(projects.notification)
	implementation(projects.common)
}