<?php
$response = array();

if (isset($_POST['note_id'])) {
    require 'db_connect.php';

    $db = new DB_CONNECT();
    $con = $db->con;

    $note_id = $_POST['note_id'];

    $result = $con->query("DELETE FROM notes WHERE id = '$note_id'");

    if ($result) {
        $response["success"] = 1;
        $response["message"] = "Note deleted successfully.";
    } else {
        $response["success"] = 0;
        $response["message"] = "Error deleting note.";
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required fields are missing.";
}

echo json_encode($response);
?>