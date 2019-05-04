
$( document ).ready(function() {
    $(".newNoteButton").click(function() {navToNote("new")});
});

function getCurrentLanguage() {
    console.log("the thing" + $("#currentLanguage").html());
    return $("#currentLanguage").html();
}

function navToNote(id) {
    // generates the address to navigate to
    const location = "noteEditor?id=" + id + "?currentLanguage=" + getCurrentLanguage();
    // navigates there
    window.location = location;
}


