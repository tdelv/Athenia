<#assign pagecontent>

    <div class="container-fluid">

        <div class="d-sm-flex align-items-baseline justify-content-between mb-4">
            <h1 class="h3 mb-0 text-gray-800">Review Session</h1>
            <h1 class="h6"><span class="font-weight-bold">Starting Date:</span> ${startDate}</h1>
            <h1 class="h6"><span class="font-weight-bold">Ending Date:</span> ${endDate}</h1>
            <h1 class="h6"><span class="font-weight-bold">Tags:</span> ${tagSelection}</h1>
        </div>
    </div>

    <div class="d-none" id="startDate">${startDate}</div>
    <div class="d-none" id="endDate">${endDate}</div>
    <div class="d-none" id="tagSelection">${tagSelection}</div>

</#assign>

<#assign script>
    <script src="js/reviewing.js"></script>
</#assign>

<#include "template.ftl">