# دليل استخدام Swagger API Documentation

## 📚 نظرة عامة
تم إعداد Swagger (OpenAPI 3.0) لتوثيق جميع APIs في مشروع دنانيرك مع دعم كامل لـ JWT Authentication.

## 🚀 كيفية الوصول إلى Swagger UI

بعد تشغيل التطبيق، يمكنك الوصول إلى Swagger UI من خلال:

```
http://localhost:8080/swagger-ui.html
```

أو:

```
http://localhost:8080/swagger-ui/index.html
```

## 🔐 كيفية استخدام Authentication في Swagger

### الخطوة 1: تسجيل الدخول
1. افتح Swagger UI
2. ابحث عن endpoint: `POST /api/auth/signin`
3. اضغط على "Try it out"
4. أدخل بيانات تسجيل الدخول:
```json
{
  "username": "your_username",
  "password": "your_password"
}
```
5. اضغط "Execute"
6. انسخ قيمة `token` من الاستجابة (Response)

### الخطوة 2: إضافة Token للـ Authorization
1. اضغط على زر **"Authorize"** 🔓 في أعلى الصفحة
2. في النافذة المنبثقة، الصق الـ token في حقل "Value"
3. اضغط "Authorize"
4. اضغط "Close"

### الخطوة 3: تجربة الـ APIs
الآن يمكنك تجربة أي endpoint محمي بـ JWT Authentication!

## 📋 الـ Endpoints المتاحة بدون Authentication

- `POST /api/auth/signin` - تسجيل الدخول
- `POST /api/users/register` - تسجيل مستخدم جديد
- `POST /api/users/activecode` - إرسال كود التفعيل
- `POST /api/users/forgot_password` - نسيت كلمة المرور
- `POST /api/users/reset_password` - إعادة تعيين كلمة المرور
- جميع endpoints الخاصة بالصور (`/getimage/**`)

## 🔧 الإعدادات المتقدمة

### تخصيص Swagger Configuration
يمكنك تعديل إعدادات Swagger من خلال:
- **ملف Java**: `src/main/java/com/earn/earnmoney/config/SwaggerConfig.java`
- **ملف Properties**: `src/main/resources/application.properties`

### إضافة توثيق لـ Controller جديد
استخدم annotations التالية:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "اسم المجموعة", description = "وصف المجموعة")
@RestController
@RequestMapping("/api/example")
public class ExampleController {
    
    @Operation(
        summary = "عنوان قصير للـ endpoint",
        description = "وصف تفصيلي للـ endpoint",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        // ...
    }
}
```

## 📖 الوصول إلى OpenAPI JSON/YAML

- **JSON Format**: `http://localhost:8080/v3/api-docs`
- **YAML Format**: `http://localhost:8080/v3/api-docs.yaml`

## ⚙️ تشغيل المشروع

تأكد من:
1. تشغيل قاعدة البيانات PostgreSQL على المنفذ 5433
2. تشغيل التطبيق Spring Boot
3. الوصول إلى Swagger UI من المتصفح

## 🛠️ استكشاف الأخطاء

### المشكلة: لا يمكن الوصول إلى Swagger UI
**الحل**: تأكد من:
- التطبيق يعمل على المنفذ 8080
- تم تحميل جميع dependencies من Maven
- لا يوجد أخطاء في console

### المشكلة: الـ APIs تعطي 401 Unauthorized
**الحل**: 
- تأكد من إضافة JWT token في زر "Authorize"
- تأكد من صحة الـ token (لم ينتهي صلاحيته)
- تأكد من نسخ الـ token كاملاً بدون مسافات

### المشكلة: بعض الـ endpoints لا تظهر
**الحل**:
- تأكد من أن الـ Controller موجود في package `com.earn.earnmoney`
- أعد تشغيل التطبيق
- امسح cache المتصفح

## 📝 ملاحظات مهمة

1. **الأمان**: لا تشارك JWT tokens مع أحد
2. **الصلاحية**: الـ token صالح لمدة أسبوع (604800000 ms)
3. **Refresh Token**: استخدم endpoint `/api/auth/refresh` لتجديد الـ token
4. **البيئة الإنتاجية**: يُنصح بتعطيل Swagger في الإنتاج أو حمايته بكلمة مرور

## 🎯 نصائح للاستخدام الأمثل

1. استخدم خاصية "Try it out" لتجربة الـ APIs مباشرة
2. اقرأ وصف كل endpoint قبل استخدامه
3. تحقق من نوع البيانات المطلوبة (Request Body Schema)
4. راجع الاستجابات المتوقعة (Response Schema)
5. استخدم خاصية "Filter" للبحث عن endpoints معينة

---

تم إعداد هذا التوثيق بواسطة فريق دنانيرك 🚀
