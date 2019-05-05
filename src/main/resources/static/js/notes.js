$(document).ready(function() {
   getNoteList();
});

function getNoteList() {
    const postParameters = {};
    $.post("getNoteList", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);

        console.log("note object");
        console.log(responseObject);
    }) ;
}