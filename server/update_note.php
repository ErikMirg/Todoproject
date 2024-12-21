<?php
$response = array();

if (isset($_POST['note_id']) && isset($_POST['note_text'])) {
    require 'db_connect.php';

    $db = new DB_CONNECT();
    $con = $db->con;

    $note_id = $_POST['note_id'];
    $note_text = $_POST['note_text'];

    $stmt = $con->prepare("UPDATE notes SET note = '$note_text' WHERE id = '$note_id'");

    if ($stmt->execute()) {
        $response["success"] = 1;
        $response["message"] = "Note updated successfully.";
    } else {
        $response["success"] = 0;
        $response["message"] = "Error updating note.";
    }

    $stmt->close();
} else {
    $response["success"] = 0;
    $response["message"] = "Required fields are missing.";
}

echo json_encode($response);
?>