<?php
$response = array();

if (isset($_POST['note_id']) && isset($_POST['enabled'])) {
    require 'db_connect.php';

    $db = new DB_CONNECT();
    $con = $db->con;

    $note_id = intval($_POST['note_id']);
    $enabled = intval($_POST['enabled']); 

    $query = "UPDATE notes SET enabled = $enabled WHERE id = $note_id";
    $result = $con->query($query);

    if ($result) {
        $response["success"] = 1;
        $response["message"] = "Note updated successfully.";
    } else {
        $response["success"] = 0;
        $response["message"] = "Error updating note.";
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required fields are missing.";
}

echo json_encode($response);
?>