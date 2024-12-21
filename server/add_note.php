<?php
$response = array();

if (isset($_POST['user_id']) && isset($_POST['note'])) {
    require 'db_connect.php';

    $db = new DB_CONNECT();
    $con = $db->con;

    $user_id = $_POST['user_id'];
    $note = $_POST['note'];

    $result = $con->query("INSERT INTO notes (user_id, note, enabled) VALUES ('$user_id', '$note', true)");

    if ($result) {
        $response["success"] = 1;
        $response["message"] = "Note added successfully.";
    } else {
        $response["success"] = 0;
        $response["message"] = "Error adding note.";
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required fields are missing.";
}

echo json_encode($response);
?>