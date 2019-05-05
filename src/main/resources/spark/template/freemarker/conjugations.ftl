<#assign pagecontent>
    <!-- Begin Page Content -->
    <div class="container-fluid">

        <!-- Page Heading -->
        <div class="d-sm-flex align-items-center justify-content-between mb-4">
            <h1 class="h3 mb-0 text-gray-800">Conjugations</h1>
            <button type="button" id="addConjButton" class="btn btn-primary">Add new conjugation</button>
        </div>

        <!-- Content Row -->
        <div class="row">
            <div class="col" id="conjugationContainer">

            </div>
        </div>

    </div>
    <!-- /.container-fluid -->
</#assign>

<#assign script>
    <script src="js/conjugations.js"></script>
</#assign>

<#include "template.ftl">