// TODO: for all add/delete/update post requests, send the current note id

let freeNoteId = null;
let freeNote = null;

$( document ).ready(function() {

    // can't get this working :--(
    let tt = new Tooltip($("#insertTextButton"), {
        placement: "right",
        title: "Top",
        trigger: "hover"
    });

    freeNoteId = $(".invisible").html();

    const freeNoteObject = $("#freeNote").html();
    freeNote = JSON.parse(freeNoteObject);

    renderModules();

    //console.log("free note: " + $("#freeNote"));
    //console.log("free note name: " + $("#freeNote").html().noteTitle);

    // BUTTON HANDLERS
    $("#insertTextButton").click(insertNote);
    $("#insertVocabButton").click(insertVocab);
    $("#insertConjugationButton").click(insertConjugation);
    $("#insertExclamationButton").click(insertExclamation);
    $("#insertQuestionButton").click(insertQuestion);

    $("#notePageTitle").blur(updateNoteTitle);

});

function renderModules() {

    const modules = freeNote.moduleContent;

    for (let i = 0; i < modules.length; i++) {
        let currentModule = modules[i];
        let id = modules[i].id;
        let html;
        if (moduleMap.has(id)) {
            // it wont for right now
            let mod = moduleMap.get(id);
            html = mod.toHTML();
        } else {

            let newMod;

            switch(currentModule.modtype) {
                case "NOTE":
                    newMod = new Note(currentModule);
                    break;
                case "VOCAB":
                    newMod = new Vocabulary(currentModule);
                    break;
                case "ALERT_EXCLAMATION":
                    newMod = new Exclamation(currentModule);
                    break;
                case "QUESTION":
                    newMod = new Question(currentModule);
                    break;
                default:
                    console.log("uhhh");
            }

            html = newMod.toHTML();
        }
        $("#noteBody").append(html);

    }
}

function updateNoteTitle() {

    const newTitle = $(this).val();

    const postParameters = {freeNoteId: getFreeNoteId(), newTitle: newTitle};
    $.post("/updateNoteTitle", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            // yay
        } else {
            console.log("message: " + responseObject.message);
        }
    });

    //TODO: post request

}

function getFreeNoteId() {
    return freeNoteId;
}

function insertModule(module) {
    $("#noteBody").append(module.toHTML());
    module.setUp();
}

function insertNote() {
    console.log("inserting note");
    const postParameters = {noteString: "note content", freeNoteId: getFreeNoteId()};
    $.post("noteAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newNoteData = responseObject.newNoteModule;
            let newNoteModule = new Note(newNoteData);
            // newNoteModule = new Note(newNote.id, newNote.dateCreated, newNote.dateModified, newNote.noteContent);
            insertModule(newNoteModule);
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}

function removeNote(id) {
    const noteIdStr = "#" + id;
    const postParameters = {idToRemove : id, freeNoteId: getFreeNoteId()};
    $.post("noteRemover", postParameters, responseJSON => {
       const responseObject = JSON.parse(responseJSON);
       if (responseObject.successful) {
           $(noteIdStr).remove();
       } else {
           console.log(responseObject.message);
       }
    });
}

function removeExclamation(id) {
    const exclamationIdStr = "#" + id;
    const postParameters = {alertId : id, freeNoteId : getFreeNoteId()};
    $.post("alertRemove", postParameters, responseJSON => {
       const responseObject = JSON.parse(responseJSON);
       if (responseObject.successful) {
           $(exclamationIdStr).remove();
       } else {
           console.log(responseObject.message);
       }
    });

}

function insertVocab() {
    console.log("inserting vocab");
    const postParameters = {newTerm: "term", newDef: "definition", freeNoteId: getFreeNoteId()};
    $.post("/vocabularyAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newVocabData = responseObject.newVocabModule;
            const newVocabModule = new Vocabulary(newVocabData);
            insertModule(newVocabModule);
            newVocabModule.setUp();
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}

function insertConjugation() {
    console.log("inserting conjugation");
    const postParameters = {};
    $.post("/conjugationAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newConjugation = responseObject.newConjugationModule;
            const newConjugationModule = new ConjugationTable(newConjugation.id, newConjugation.dateCreated, newConjugation.dateModified, newConjugation.header, new List());
            insertModule(newConjugationModule);
        } else {
            console.log("message: " + responseObject.message);
        }
    });

}

function insertExclamation() {
    console.log("inserting exclamation");
    const postParameters = {alertString: "content", freeNoteId: getFreeNoteId()};
    $.post("alertAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newExclamationData = responseObject.newAlertModule;
            const newExclamationModule = new Exclamation(newExclamationData);
            insertModule(newExclamationModule);
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}

function insertQuestion() {
    console.log("inserting question");
    const postParameters = {questionString: "content", freeNoteId: getFreeNoteId()};
    $.post("questionAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newQuestionData = responseObject.newQuestionModule;
            const newQuestionModule = new Question(newQuestionData);
            insertModule(newQuestionModule);
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}