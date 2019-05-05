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
    }

    setUp() {
        const selector = "#" + this.id;
        $(selector).hover(Module.prototype.onModuleHover, Module.prototype.onModuleLeave);
        $(selector).click(Module.prototype.onModuleHover);
    }

    onModuleHover() {
        // slight shadow background
        // TODO: use animation instead of css
        const selector = "#" + this.id;
        $(selector).css("backgroundColor", "#f2f2f2");
        document.body.style.cursor = "pointer";
    }

    onModuleLeave() {
        const selector = "#" + this.id;
        $(selector).css("backgroundColor", "white");
        document.body.style.cursor = "default";
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
        return `<p id=\"${this.id}\">!!! ${this.content}</p>`;
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

class ConjugationTable extends Module {
    constructor(id, dateCreated, dateModified, pairList, tableHeight) {
        super(id, dateCreated, dateModified);
        this.pairList = pairList;
        this.tableHeight = tableHeight;
    }

    toHTML() {
        return `<p id=\"${this.id}\">${this.pairList}</p>`;
    }
}