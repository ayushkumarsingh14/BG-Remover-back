# 🖼️ Background Remover Web App
[![Live Demo](https://img.shields.io/badge/Live-Demo-brightgreen)](https://bg-remover-rouge-eta.vercel.app/)


An **AI-powered full-stack web application** that removes image backgrounds instantly.  
Integrated with **ClipDrop API** for background removal and **Razorpay** for secure payments.  
Built with **React.js (Frontend)**, **Spring Boot (Backend)**, **MySQL (Database)**, and **Docker**.

---

## 🚀 Features

- ✨ **AI-Based Background Removal** using ClipDrop API.  
- 💳 **Payment Integration** with Razorpay.  
- 🔐 **Secure Authentication** using Spring Security & JWT.  
- 🐳 **Dockerized Backend** connected to MySQL database.  
- 📱 **Responsive Frontend** built with React.js and Tailwind CSS.  
- ⚙️ **RESTful APIs** for image processing and user management.

---

## 🛠️ Tech Stack

**Frontend:** React.js, Tailwind CSS  
**Backend:** Spring Boot, Java, Spring Security, JWT  
**Database:** MySQL  
**Containerization:** Docker  
**Payment Gateway:** Razorpay  
**AI API:** ClipDrop API  

---

## ⚙️ Installation & Setup

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/ayushkumarsingh14/BG-Remover-back/
cd background-remover
```

### 2️⃣ Backend Setup (Spring Boot)

1. Open the backend folder in your IDE (like IntelliJ IDEA or Eclipse).  
2. Configure `application.properties` with your MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/background_remover
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

Add your API keys in environment variables or a config file:

```properties

clipdrop.api.key=YOUR_CLIPDROP_API_KEY
razorpay.key.id=YOUR_RAZORPAY_KEY_ID
razorpay.key.secret=YOUR_RAZORPAY_SECRET
```

Build and run the backend:

```properties

mvn clean install
mvn spring-boot:run
```

### 3️⃣ Docker Setup (Optional)

To run backend in a Docker container:

```properties
docker build -t background-remover-backend .
docker run -p 8080:8080 background-remover-backend
```

## 🧠 Usage

1. Upload an image from your device.  
2. The app removes the background using **ClipDrop API**.  
3. Pay securely via **Razorpay** to download the final image.  
4. Manage your history and payments securely.
5. 
---

## 🧾 API References

- [ClipDrop API](https://clipdrop.co/apis)  
- [Razorpay API](https://razorpay.com/docs/api/)

---


## 👨‍💻 Author

**Ayush Kumar Singh**  
[GitHub Profile](https://github.com/ayushkumarsingh14)

---

⭐ **If you like this project, give it a star on GitHub!**

---
