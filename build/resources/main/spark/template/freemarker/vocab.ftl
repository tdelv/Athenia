<#assign pagecontent>
    <!-- Begin Page Content -->
    <div class="container-fluid">

        <!-- Page Heading -->
        <div class="d-sm-flex align-items-center justify-content-between mb-4">
            <h1 class="h3 mb-0 text-gray-800">${title}</h1>
            <button type="button" id="addVocab" class="btn btn-primary">Add new vocabulary</button>
        </div>

        <!-- Content Row -->
        <div class="row">
            <div class="col" id="vocabularyContainer">

            </div>
        </div>

    </div>
    <!-- /.container-fluid -->
</#assign>

<#assign script>
    <script src="js/vocab.js"></script>
</#assign>

<#include "template.ftl">