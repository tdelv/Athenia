
// TODO: display recent notes on homepage/dashbaord
// TODO: when an old note is opened, render all the modules

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
            console.log("modmap size: " + moduleMap.size);
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

        const tagSelect = "#tags-" + thisOlThing.id;
        const newTagContent = $(tagSelect).val();
        console.log(newTagContent);

        const ratingSelect = "#rating-" + thisOlThing.id;
        const newRatingContent = $(ratingSelect).val();
        console.log(newRatingContent);

        this.tags = newTagContent;
        this.rating = newRatingContent;

        const postParameters = {noteId: thisOlThing.id,
                                noteUpdate: newNoteContent,
                                tagUpdate: newTagContent,
                                ratingUpdate: newRatingContent};
        $.post("/noteUpdate", postParameters, responseJSON => {
            const responseObject = JSON.parse(responseJSON);
            if (responseObject.successful) {
                const updatedNote = responseObject.updatedNote;
                console.log("new content " + updatedNote.content);
            } else {
                console.log("message: " + responseObject.message);
            }
        });
    }

    toHTML() {

        const input = `<input type="text" class="form-control mb-3" placeholder="${this.content}">`;
        const div = `<div class="noteModule col">${input}</div>`;
        const remover = `<div class="col-sm-auto p-2 d-flex justify-content-end">
                        <i class="fa fa-trash" onclick="removeNote('${this.id}')"></i></div>`;

        const tags = `<div class="col"><input type="text" id='tags-${this.id}' class="form-control" 
                placeholder="Type in tags separated by comma (,)"></div>`;

        const rating = `<div class="col">
                <select class="form-control" id='rating-${this.id}' name="ratingSelect-${this.id}">
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                </select></div>`;

        const container = `<div class="container-fluid" id="${this.id}">
                        <div class="row">${div}${tags}${rating}${remover}</div></div>`;
        return container;
    }

}

class Exclamation extends Note {
    constructor(newExclamationData) {
        super(newExclamationData);
    }

    update(thisOlThing) {
        const selector = "#" + thisOlThing.id + " input";
        const newContent = $(selector).val();

        const postParameters = {alertId: thisOlThing.id, alertUpdate: newContent};
        $.post("/alertUpdate", postParameters, responseJSON => {
            const responseObject = JSON.parse(responseJSON);
            if (responseObject.successful) {
                const updatedAlert = responseObject.updatedAlert;
                console.log("new content " + updatedAlert.content);
            } else {
                console.log("message: " + responseObject.message);
            }
        });
    }

    toHTML() {
        const icon = "<i class=\"fa fa-exclamation\"></i>";
        const input = `<input type="text" class="form-control mb-3 ml-2 d-inline w-75" placeholder="${this.content}">`;
        const div = `<div class="noteModule col">${icon} ${input}</div>`;
        const remover = `<div class="col-sm-auto p-2 d-flex justify-content-end">
                        <i class="fa fa-trash" onclick="removeExclamation('${this.id}')"></i></div>`;
        const container = `<div class="container-fluid" id="${this.id}">
                        <div class="row">${div}${remover}</div></div>`;

        return container;
    }

}

class Question extends Note {
    constructor(newQuestionData) {
        super(newQuestionData);
    }

    update(thisOlThing) {
        const selector = "#" + thisOlThing.id + " input";
        const newContent = $(selector).val();

        const postParameters = {questionId: thisOlThing.id, questionUpdate: newContent};
        $.post("/questionUpdate", postParameters, responseJSON => {
            const responseObject = JSON.parse(responseJSON);
            if (responseObject.successful) {
                const updatedQuestion = responseObject.updatedQuestion;
                console.log("new content " + updatedQuestion.content);
            } else {
                console.log("message: " + responseObject.message);
            }
        });
    }

    toHTML() {
        const icon = `<i class="fa fa-question"></i>`;
        const input = `<input type="text" class="form-control mb-3 ml-1 d-inline w-75" placeholder="${this.content}">`;
        const div = `<div class="noteModule col">${icon} ${input}</div>`;
        const remover = `<div class="col-sm-auto p-2 d-flex justify-content-end">
                        <i class="fa fa-trash" onclick="removeQuestion('${this.id}')"></i></div>`;
        const container = `<div class="container-fluid" id="${this.id}">
                        <div class="row">${div}${remover}</div></div>`;
        return container;
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

    // TODO: attach this handler to onBlur
    update(thisOlThing) {

        // if need be, id can be stored in the event target. event would be parameter to this method

        // TODO: store the new values in this object if that hasn't been done already

        const newTerm = $("input[name='updatedTerm']").val();
        const newDef = $("input[name='updatedDef']").val();
        const newRating = $("input[name='updatedRating']").val();

        thisOlThing.term = newTerm;
        thisOlThing.def = newDef;
        thisOlThing.rating = newRating;

        const postParameters = {vocabId: thisOlThing.id, updatedTerm: newTerm, updatedDef: newDef, updatedRating: newRating, freeNoteId: getFreeNoteId()};
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

        console.log("this: " + this.id);

        const form = `<form id="${this.id}" name="form-${this.id}" class="mb-3" action="/vocabularyUpdate" 
                        method="post">` +
            `<div class="input-group">` +
            `<input type="text" name="vocabId" value="${this.id}" style="display:none">` +
            `<input type="text" name="updatedTerm" class="form-control" placeholder="${this.term}">` +
            `<input type="text" name="updatedDef" class="form-control" placeholder="${this.def}">` +
            `<input type="text" name="updatedRating" class="form-control" placeholder="${this.rating}">` +

            // TODO somehow allow for rating changes
            //`<input type="submit" id="" class="btn btn-primary vocabSubmit" value="Save"/>` +
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
        //form += `<button type="submit" id="sub-butt-${this.id}" class="btn conjSave btn-primary mt-2">Save</button>`;

        form += `</div></form>`;

        return form;

    }



    toMap() {
        return JSON.stringify(this);
    }
}