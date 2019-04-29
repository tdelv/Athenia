<#assign content>

    <h1>Which language are you studying today?</h1>

    <#list languages as l>
        <div class="card mb-4">
            <div class="card-header">
                ${l}
            </div>
            <div class="card-body">
                Mama mia
            </div>
        </div>
    </#list>

    <#-- plus and minus options -->



</#assign>

<#include "main.ftl">