
// TODO: add modtype!!!!!!

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
    constructor(id, dateCreated, dateModified, content) {
        super(id, dateCreated, dateModified);
        this.content = content;
    }

    toHTML() {
        return `<p id=\"${this.id}\">${this.content}</p>`;
    }
}

class Exclamation extends Note {
    constructor(id, dateCreated, dateModified, content) {
        super(id, dateCreated, dateModified, content);
    }

    toHTML() {

        const icon = "<i class=\"fa fa-exclamation\"></i>";

        const content = `<span class="editable">${this.content}</span>`;

        const div = `<div class="noteModule" id="${this.id}">${icon} ${content}</div>`;

        return div;
    }

}

class Question extends Note {
    constructor(id, dateCreated, dateModified, content) {
        super(id, dateCreated, dateModified, content);
    }

    toHTML() {
        return `<p id=\"${this.id}\">??? ${this.content}</p>`;
    }
}

class Vocabulary extends Module {
    constructor(id, dateCreated, dateModified, term, def) {
        super(id, dateCreated, dateModified);
        this.term = term;
        this.def = def;
    }

    toHTML() {
        return `<p id=\"${this.id}\">${this.term} ${this.def}</p>`;
    }
}

/**
 * pairList is a list of pairs, which is a list of length 2
 */
class ConjugationTable extends Module {
    constructor(id, dateCreated, dateModified, header, pairList, tableHeight) {
        super(id, dateCreated, dateModified);
        this.header = header;
        this.pairList = pairList;
        this.tableHeight = tableHeight;
    }

    toHTML() {

        let htmlString = "";

        for (let i = 0; i < this.pairList.length; i++) {
            htmlString = htmlString + `<p id=\"${this.id}\">${this.pairList[i]}</p>`
        }

        return htmlString;
    }

    toMap() {
        return JSON.stringify(this);
    }
}