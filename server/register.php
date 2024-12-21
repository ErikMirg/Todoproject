<?php
$response = array();

if (isset($_POST['username']) && isset($_POST['password'])) {
    require 'db_connect.php';

    $db = new DB_CONNECT();
    $con = $db->con;

    $username = $_POST['username'];
    $password = password_hash($_POST['password'], PASSWORD_BCRYPT);

    $result = $con->query("INSERT INTO users (username, password) VALUES ('$username', '$password')");

    if ($result) {
        $response["success"] = 1;
        $response["message"] = "Registration successful.";
    } else {
        $response["success"] = 0;
        $response["message"] = "Error during registration.";
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required fields are missing.";
}

echo json_encode($response);
?>