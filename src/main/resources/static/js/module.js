
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