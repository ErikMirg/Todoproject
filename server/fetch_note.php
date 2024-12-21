<?php
$response = array();

if (isset($_POST['user_id'])) {
    require 'db_connect.php';

    $db = new DB_CONNECT();
    $con = $db->con;

    $user_id = $_POST['user_id'];

    $result = $con->query("SELECT * FROM notes WHERE user_id = '$user_id'");

    if ($result->num_rows > 0) {
        $response["success"] = 1;
        $response["notes"] = array();

        while ($row = $result->fetch_assoc()) {
            $note = array();
            $note["id"] = $row["id"];
            $note["note"] = $row["note"];
            $note["enabled"] = $row["enabled"];

            array_push($response["notes"], $note);
        }
    } else {
        $response["success"] = 0;
        $response["message"] = "No notes found.";
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required fields are missing.";
}

echo json_encode($response);
?>