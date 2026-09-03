```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>
    <form action="/api/language/set" method="post">
        <select name="language" onchange="this.form.submit()">
            <option value="en">English</option>
            <option value="fr">Français</option>
            <option value="es">Español</option>
            <!-- Add more languages as needed -->
        </select>
    </form>
</body>
</html>
```