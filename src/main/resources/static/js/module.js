

// TODO: update vocab the same way you update text
// TODO: set up link on dashbord
// TODO: display recent notes on homepage/dashbaord

// TODO: add modtype!!!!!!
// TODO: when an input is not being edited, remove the border

const moduleMap = new Map();

/*
 * THE BIG DADDY
 */
class Module {

    /**
     * a constructor for a Module
     * @param id the module's id, generated in backend
     * @param dateCreated
     * @param dateModified
     */
    constructor(id, dateCreated, dateModified) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        if (moduleMap.has(id)) {
            console.log("a module with id:" + id + " already exists");
        } else {
            moduleMap.set(id, this);
        }
    }

    setUp() {

        const alert = this;

        const selector = "#" + this.id;
        $(selector).hover(this.onModuleHover, this.onModuleLeave);
        $(selector).click(this.onModuleHover);

        const editableSelector = selector + " .editable";

        $(editableSelector).hover(this.onEditHover, this.onEditLeave);
        //$(editableSelector).click(function(){this.onEditClick(alert)});
    }

    onEditHover() {
        document.body.style.cursor = "pointer";
    }

    onEditLeave() {
        document.body.style.cursor = "default";
    }

    /*
    onEditClick(alert) {

        //TODO: this should be moved to the alert class

        const ph = $(this).val();

        const html = `<input type="text" name="edit" placeholder="${ph}" class="userInput">`;

        $(alert).append(html);

        $(this).remove();

        $(".userInput").keyup(function(event){
            if (event.which == 13) {
                const value = $(this).val();

                const postParameters = {alertId: alert.id, alertUpdate: value};
                $.post("/alertUpdate", postParameters, responseJSON => {
                    const responseObject = JSON.parse(responseJSON);
                    if (responseObject.successful) {
                        alert.content = responseObject.updatedAlert.alertContent;

                    } else {
                        console.log("message: " + responseObject.message);
                    }
                });

            }
        });

    }
    */

    onModuleHover() {
        // slight shadow background
        // TODO: use animation instead of css
        const selector = "#" + this.id;
        $(selector).css("backgroundColor", "#f2f2f2");
    }

    onModuleLeave() {
        const selector = "#" + this.id;
        $(selector).css("backgroundColor", "white");
    }

    onModuleClick() {
        // outline
    }
}

class Note extends Module {

    constructor(newNoteData) {
        super(newNoteData.id, newNoteData.dateCreated, newNoteData.dateModified);
        this.content = newNoteData.content;
        this.setUp();
    }

    setUp() {
        const selector = "#" + this.id + " input";
        const thisOlThing = this;
        $(selector).blur(function(){thisOlThing.update(thisOlThing);});
    }

    update(thisOlThing) {
        const selector = "#" + thisOlThing.id + " input";
        const newNoteContent = $(selector).val();

        const postParameters = {noteId: thisOlThing.id, noteUpdate: newNoteContent};
        $.post("/noteUpdate", postParameters, responseJSON => {
            const responseObject = JSON.parse(responseJSON);
            if (responseObject.successful) {
                // TODO: check that responseObject.updatedNote matches this object
                console.log("new content " + responseObject.updatedNote.noteContent);
            } else {
                console.log("message: " + responseObject.message);
            }
        });
    }

    toHTML() {
        const input = `<input type="text" class="form-control mb-3" placeholder="${this.content}">`;
        const div = `<div class="noteModule" id="${this.id}">${input}</div>`;
        return div;
    }
}

class Exclamation extends Note {
    constructor(newExclamationData) {
        super(newExclamationData);
    }

    toHTML() {
        const icon = "<i class=\"fa fa-exclamation\"></i>";
        const input = `<input type="text" class="form-control mb-3 ml-2 d-inline w-75" placeholder="${this.content}">`;
        const div = `<div class="noteModule" id="${this.id}">${icon} ${input}</div>`;
        return div;
    }

}

class Question extends Note {
    constructor(newQuestionData) {
        super(newQuestionData);
    }

    toHTML() {
        const icon = `<i class="fa fa-question"></i>`;
        const input = `<input type="text" class="form-control mb-3 ml-1 d-inline w-75" placeholder="${this.content}">`;
        const div = `<div class="noteModule" id="${this.id}">${icon} ${input}</div>`;
        return div;
    }
}

class Vocabulary extends Module {
    constructor(newVocabData) {
        super(newVocabData.id, newVocabData.dateCreated, newVocabData.dateModified);
        this.term = newVocabData.term;
        this.def = newVocabData.def;
        this.rating = 1;
    }

    setUp() {
        const selector = "#" + this.id + " input";
        const thisOlThing = this;
        $(selector).blur(function(){thisOlThing.update(thisOlThing);});
    }

    update(thisOlThing) {
        // TODO:
        // for each text input, store the value in this object
        // use a post request and these new values to update this module in the backend

    }

    /*
        update(thisOlThing) {
            const selector = "#" + thisOlThing.id + " input";
            const newNoteContent = $(selector).val();

            const postParameters = {noteId: thisOlThing.id, noteUpdate: newNoteContent};
            $.post("/noteUpdate", postParameters, responseJSON => {
                const responseObject = JSON.parse(responseJSON);
                if (responseObject.successful) {
                    // TODO: check that responseObject.updatedNote matches this object
                    console.log("new content " + responseObject.updatedNote.noteContent);
                } else {
                    console.log("message: " + responseObject.message);
                }
            });
        }


        */

    // TODO: attach this handler to onBlur
    updateVocabularyModule() {

        // if need be, id can be stored in the event target. event would be parameter to this method

        // TODO: store the new values in this object if that hasn't been done already

        const freeNoteId = "TODO";

        const postParameters = {vocabId: this.id, updatedTerm: this.term, updatedDef: this.def, updatedRating: this.rating, freeNote: freeNoteId};
        $.post("/vocabularyUpdate", postParameters, responseJSON => {
            const responseObject = JSON.parse(responseJSON);
            if (responseObject.successful) {
                // TODO: nothing?

            } else {
                console.log("message: " + responseObject.message);
            }
        });

    }

    toHTML() {

        const form = `<form id="${this.id}" name="form-${this.id}" class="mb-3" action="/vocabularyUpdate" 
                        method="post">` +
            `<div class="input-group">` +
            `<input type="text" name="vocabId" value="${this.id}" style="display:none">` +
            `<input type="text" name="updatedTerm" class="form-control" placeholder="${this.term}">` +
            `<input type="text" name="updatedDef" class="form-control" placeholder="${this.def}">` +
            `<input type="text" name="updatedRating" class="form-control" placeholder="${this.rating}">` +

            // TODO somehow allow for rating changes
            `<input type="submit" id="" class="btn btn-primary vocabSubmit" value="Save"/>` +
            `</div>` +
            `<small>Date Modified: ${this.dateModified}</small>` +
            `</form>`;

        return form;
    }

}


/**
 * pairList is a list of pairs, which is a list of length 2
 */
class ConjugationTable extends Module {
    constructor(id, dateCreated, dateModified, header, pairList) {
        super(id, dateCreated, dateModified);
        this.header = header;
        this.pairList = pairList;
    }

    toHTML() {

        // TODO GET BACK TO THIS AFTER VOCAB HAS BEEN WORKED OUT
        let form = `<form id="{this.id}" name="form-${this.id}" action="/" class="\mb-5\">` +
            // form group
            `<div class=\"form-group\">`;

        // ADD THE HEADER & DATE INFORMATION
        form += `<div class=\"input-group\">` +
            `<input type=\"text\" class=\"form-control mb-1\" placeholder=\"${this.header}\"></div>` +
            `<small style="float: left;">Date Modified: ${this.dateModified}</small>` +
            `<small style="float: right">Date Created: ${this.dateCreated}</small>` +
            `</div><div class=\"form-group\">`;

        // ADD ALL THE PAIRS
        for (let i = 0; i < this.pairList.length; i++) {
            const currPair = this.pairList[i];
            form += `<div class=\"input-group\">`
            form += `<input type=\"text\" class=\"form-control\" placeholder=\"${currPair[0]}\">` +
                `<input type=\"text\" class=\"form-control\" placeholder=\"${currPair[1]}\"></div>`;
        }

        // ADD THE SUBMIT BUTTON FOR EDITS
        form += `<button type="submit" id="sub-butt-${this.id}" class="btn conjSave btn-primary mt-2">Save</button>`;

        form += `</div></form>`;

        return form;

    }



    toMap() {
        return JSON.stringify(this);
    }
}