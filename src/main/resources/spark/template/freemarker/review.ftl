<#assign pagecontent>

    <div class="container-fluid">

        <div class="d-sm-flex align-items-center justify-content-between mb-4">
            <h1 class="h3 mb-0 text-gray-800">Review</h1>
        </div>

        <!-- Content Row -->
        <div class="row">
            <div class="col">
                <p> to do :-) </p>
                <ol>
                    <#list allTags as tag>
                        <li>${tag}</li>
                    </#list>
                </ol>
            </div>
        </div>

    </div>

</#assign>

<#assign script>
    <script src="js/review.js"></script>
</#assign>

<#include "template.ftl">