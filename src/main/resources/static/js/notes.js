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

function noteToHTML(note) {

    console.log(note);

    const innerHTML = `<h3 class="card-title">${note.noteTitle}</h3>`;

    let tagHTML = `<p class="card-text">TAGS: `;
    for (let i = 0; i < note.tags.length; i++) {
        tagHTML += `${note.tags[i]}, `;
    }
    tagHTML += `</p>`;

    const cardFooter = `<div class="card-footer text-muted">Date Modified: ${note.dateModified}</div>`;
    const trashIcon = `<i class="fa fa-trash" style="position:absolute; top:25px; right: 25px;"></i>`;

    return `<div class="card mb-3" onclick="navToNote('${note.noteId}')">
            <div class="card-body noteCard" id="${note.noteId}">${innerHTML}
            ${tagHTML}${trashIcon}${cardFooter}</div></div>`;
}



// TODO FOR MIA :::: : : : :: : :: CALL navToNote(id) ~U~ found in nav.js - from jsdin
//                   click handler for note individual cards on View Notes page

// TODO ALSO DELETE METHOD FOR CLICKING ON DELETE THING


// TODO add an event
