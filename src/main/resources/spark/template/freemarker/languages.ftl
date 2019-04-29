<#assign content>

    <h1>Which language are you studying today?</h1>

    <div class="row">
        <#list languages as l>
            <div class="card mb-4">
                <div class="card-body">
                    ${l}
                </div>
            </div>
        </#list>
    </div>



    <div class="row">
        <a href="#" class="btn btn-primary btn-circle btn-lg" data-toggle="modal" data-target="#addLanguageModal">
            <i class="fa fa-plus"></i>
        </a>
        <a href="#" class="btn btn-danger btn-circle btn-lg">
            <i class="fa fa-minus"></i>
        </a>
    </div>


    <div class="modal fade show" id="addLanguageModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" style="padding-right: 15px; display: none;" aria-modal="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">

                    <h5 class="modal-title" id="exampleModalLabel">Add a Language</h5>
                    <button class="close" type="button" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">×</span>
                    </button>

                </div>
                <div class="modal-body">

                    <input type="text" placeholder="language" id="languageInput"></input>

                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" type="button" data-dismiss="modal">Cancel</button>
                    <a class="btn btn-primary" href="#" id="addLanguageButton">Add</a>
                </div>
            </div>
        </div>
    </div>

</#assign>

<#assign script>
    <script src="js/languages.js"></script>
</#assign>

<#include "main.ftl">