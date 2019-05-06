$(document).ready(function() {
   getNoteList();
});

function getNoteList() {
    const postParameters = {};
    $.post("getNoteList", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);

        // if successful, then proceed with presenting notes
        if (responseObject.successful) {

            const allNotes = responseObject.allNotes;

            // iterate through all recent notes
            for (let i = 0; i < allNotes.length; i++) {
                $("#freeNoteContainer").append(noteToHTML(allNotes[i]));
            }

            console.log("recentNotes");
            console.log(allNotes);

        } else {
            console.log("message: " + responseObject.message);
        }
    }) ;
}

function noteToHTML(note) {

    const innerHTML = `<h3 class="card-title">${note.noteTitle}</h3>`;
    const cardFooter = `<div class="card-footer text-muted">Date Modified: ${note.dateModified}</div>`


    return `<div class="card"><div class="card-body">${innerHTML}${cardFooter}</div></div>`;

    console.log("id");
    console.log(note.noteId);
    console.log("name");
    console.log(note.noteTitle);

}