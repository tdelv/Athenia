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
        } else {
            console.log("message: " + responseObject.message);
        }
    }) ;
}


function removeFreeNote(id) {
    const idStr = "#" + id;
    console.log(idStr);
    const postParameters = {noteId : id};
    $.post("removeFreeNote", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            $(idStr).remove();
        } else {
            console.log(responseObject.message);
        }
    });
}

function noteToHTML(note) {

    const innerHTML = `<h3 class="card-title">${note.noteTitle}</h3>`;

    let tagHTML = `<p class="card-text">TAGS: `;
    for (let i = 0; i < note.tags.length; i++) {
        tagHTML += `${note.tags[i]}, `;
    }
    tagHTML += `</p>`;

    const cardFooter = `<div class="card-footer text-muted">Date Modified: ${note.dateModified}</div>`;
    const trashIcon = `<i class="fa fa-trash" onclick="removeFreeNote('${note.noteId}')"></i>`;

    return `<div id="${note.noteId}" class="container-fluid"><div class="row">
            <div class="card col mb-3" onclick="navToNote('${note.noteId}')">
            <div class="card-body noteCard">${innerHTML}
            ${tagHTML}${cardFooter}</div></div>
            <div class="col-sm-auto pt-1 d-flex justify-content-end">${trashIcon}</div></div></div>`;
}

