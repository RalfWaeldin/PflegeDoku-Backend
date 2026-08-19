# PflegeDoku-Backend



\# Pflegedoku Backend API



RESTful Backend-Service für das Pflegedokumentations-System (`pflegedoku-frontend`), basierend auf \*\*Spring Boot 3\*\* und \*\*Java 21\*\*. Der Service verarbeitet Dokumentationsdaten, bietet KI-gestützte Analysen via \*\*Spring AI (OpenAI)\*\* und kümmert sich um Validierung sowie Authentifizierung (JWT).



\---



\## 🛠 Tech Stack \& Abhängigkeiten



\* \*\*Java Version:\*\* 21

\* \*\*Framework:\*\* Spring Boot `3.3.2` (`spring-boot-starter-web`, `validation`)

\* \*\*AI Integration:\*\* Spring AI `1.0.0-M1` (`spring-ai-openai-spring-boot-starter`)

\* \*\*Security \& Auth:\*\* JJWT `0.12.5` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)

\* \*\*Konfiguration:\*\* `spring-dotenv` (Lädt `.env`-Dateien automatisch zum Start)

\* \*\*Build-Tool:\*\* Apache Maven



\---



\## 🚀 Quick Start (Lokale Entwicklung)



\### 1. Voraussetzungen



\* Installed \*\*Java 21 JDK\*\*

\* \*\*Apache Maven\*\* (oder die Verwendung des Maven Wrappers `./mvnw`)

\* Ein gültiger \*\*OpenAI API Key\*\* für KI-Funktionen



\### 2. Repository klonen



```bash

git clone \[https://github.com/](https://github.com/)RalfWaeldin/pflegedoku-backend.git

cd pflegedoku-backend

