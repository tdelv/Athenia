
$( document ).ready(function() {
    $(".newNoteButton").click(function() {navToNote("new")});
});

function navToNote(id) {
    // generates the address to navigate to
    const location = "noteEditor?id=" + id;
    // navigates there
    window.location = location;
}


