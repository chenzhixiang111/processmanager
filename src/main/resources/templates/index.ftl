<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>首页</title>
</head>
<script src="../lib/jquery-3.4.1.min.js"></script>
<body>
<h1 align="center">这是首页</h1>
<div align="center" class="box1">
    <table border="0">
        <tr>
            <th>程序名</th>
            <th>重启次数</th>
            <th>上次重启时间</th>
            <th>间隔天数</th>
            <th>状态</th>
            <th>操作</th>
        </tr>
        <#list restartJobs as item>
            <tr class="restartJobs_tr_${item_index}">
                <th>${item.jobName!}</th>
                <th>${item.jobCount!}</th>
                <th>${(item.lastJobTime?string('yyyy-MM-dd HH:mm:ss'))!}</th>
                <th>${item.intervalDays!}</th>
                <th>${item.state!}</th>
                <th><input type="button" value="删除" onclick="delJob_bt(this)"></th>
            </tr>
        </#list>
    </table>
    <p>----------------------------------------------------------------------------</p>
    <div>
        <p>添加一个定时重启的任务</p>
        <span>请输入程序全路径</span>
        <input class="exe_path" type="text">
        <span>请输入间隔时间</span>
        <input class="interval_days" type="text"> <span>天</span>
        <input type="button" value="点击添加" onclick="create_restart_bt(this)"> 第一次重启会在当前时间2秒后
    </div>
    <p>----------------------------------------------------------------------------</p>
    <h3>进程内存监控</h3>
    <table>
        <tr>
            <th>程序名</th>
            <th>重启次数</th>
            <th>上次重启时间</th>
            <th>当前内存（KB）</th>
            <th>内存阀值（KB）</th>
            <th>状态</th>
            <th>操作</th>
        </tr>
        <#list memoryJobs as item>
            <tr class="memoryJobs_tr_${item_index}">
                <th>${item.exePath!}</th>
                <th>${item.jobCount!}</th>
                <th>${(item.lastJobTime?string('yyyy-MM-dd HH:mm:ss'))!}</th>
                <th>${item.memoryValue!}</th>
                <th>${item.threshold!}</th>
                <th>${item.state!}</th>
                <th><input type="button" value="删除" onclick="delmemoryJob_bt(this)"></th>
            </tr>
        </#list>
    </table>
    <p>----------------------------------------------------------------------------</p>
    <div>
        <span>请输入程序全路径</span>
        <input class="exe_path" type="text">
        <span>请输入内存阀值</span>
        <input class="threshold" type="text">KB
        <input type="button" value="点击添加" onclick="add_memory_bt(this)">
    </div>
</div>
<p>----------------------------------------------------------------------------</p>
<p>心跳接口：/heartbeat。用POST请求。超过30秒没有收到心跳就关闭目标程序</p>

<a href="kill_process_page.html">跳转到杀进程页面</a>
</body>

<style type="text/css">
    .box1{
        margin-top: 50px;
    }
</style>

<script>
    function onoff_ck1(ckbox) {
        //在点击的时候先将按钮变为不可交互，等接口回调完毕在开放为可交互
        ckbox.disabled =  '';
        $.ajax({
            url: "onoffRestartJobs",
            data: "onoffTag="+ ckbox.checked,
            type: "GET",
            success: function(obj){
                if (obj.code === 1) {
                    alert('修改成功')
                }else {
                    alert('失败了，请刷新页面')
                }
            }
        });
        ckbox.disabled =  false;
    }

    function delJob_bt(btbox) {
        var jobName = $(btbox).parent().parent().children("th:eq(0)").text();
        $.ajax({
            url: "removeRestartJob",
            data: "jobName=" + jobName,
            type: "POST",
            success: function(obj){
                if (obj.code === 1) {
                    window.location.reload()
                }else {
                    alert('失败了，请刷新页面')
                }
            }
        });
    }

    function delmemoryJob_bt(btbox) {
        var exePath = $(btbox).parent().parent().children("th:eq(0)").text();
        $.ajax({
            url: "removeMemoryJob",
            data: "exePath=" + exePath,
            type: "POST",
            success: function(obj){
                if (obj.code === 1) {
                    window.location.reload()
                }else {
                    alert('失败了，请刷新页面')
                }
            }
        });
    }

    function create_restart_bt(btbox) {
        var exePath = $(btbox).parent().children(".exe_path").val();
        var intervalDays = $(btbox).parent().children(".interval_days").val();
        $.ajax({
            url: "createNewRestartJob",
            data: "exePath=" + exePath +"&intervalDays="+intervalDays,
            type: "POST",
            success: function(obj){
                if (obj.code === 1) {
                    window.location.reload()
                }else {
                    alert('失败了，请刷新页面')
                }
            }
        });
    }

    function add_memory_bt(btbox) {
        var exePath = $(btbox).parent().children(".exe_path").val();
        var threshold = $(btbox).parent().children(".threshold").val();
        $.ajax({
            url: "addProcessMemory",
            data: "exePath=" + exePath +"&threshold="+threshold,
            type: "POST",
            success: function(obj){
                if (obj.code === 1) {
                    window.location.reload()
                }else {
                    alert('失败了，请刷新页面')
                }
            }
        });
    }
</script>
</html>