class Module {
    constructor(id, dateCreated, dateModified) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    onModuleHover() {
        // slight shadow background
        // TODO: use animation instead of css
        console.log("hovering");
        const selector = "#" + this.id;
        $(selector).css("backgroundColor", "#f2f2f2");
        document.body.style.cursor = "pointer";
    }

    onModuleLeave() {
        console.log("left");
        const selector = "#" + this.id;
        $(selector).css("backgroundColor", "white");
        document.body.style.cursor = "default";
    }

    onModuleClick() {
        // outline
    }
}

class TextModule extends Module {
    constructor(id, dateCreated, dateModified, content) {
        super(id, dateCreated, dateModified);
        this.content = content;
    }

    toHTML() {
        return `<p id=\"${this.id}\">${this.content}</p>`;
    }

    setUp() {
        const selector = "#" + this.id;
        $(selector).hover(Module.prototype.onModuleHover, Module.prototype.onModuleLeave);
        $(selector).click(Module.prototype.onModuleHover);
    }
}

class Exclamation extends TextModule {
    constructor(id, dateCreated, dateModified, content) {
        super(id, dateCreated, dateModified, content);
    }
}

class Question extends TextModule {
    constructor(id, dateCreated, dateModified, content) {
        super(id, dateCreated, dateModified, content);
    }
}

class Vocabulary extends Module {
    constructor(id, dateCreated, dateModified, term, def) {
        super(id, dateCreated, dateModified);
        this.term = term;
        this.def = def;
    }
}

class ConjugationTable extends Module {
    constructor(id, dateCreated, dateModified, pairList, tableHeight) {
        super(id, dateCreated, dateModified);
        this.pairList = pairList;
        this.tableHeight = tableHeight;

    }
}