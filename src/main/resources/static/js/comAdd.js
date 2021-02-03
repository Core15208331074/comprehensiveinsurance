/**
 项目JS主入口
 以依赖layui的layer和form模块为例
 **/
layui.define(['layer', 'form', 'element'], function (exports) {
    var $ = layui.jquery,
        layer = layui.layer,
        form = layui.form,
        element = layui.element;//Tab的切换功能，切换事件监听等，需要依赖element模块

    $.get('/com/getComConfigData', {}, function (data) {
        if (data.data != null) {
            //串口
            let serialPortNameList = data.data.comList;
            let serialPortNameHtml = "";
            $.each(serialPortNameList, function (index, item) {
                serialPortNameHtml += " <option value='" + item + "'>" + item + "</option>";
            });
            $("select[name='serialPortName']").append(serialPortNameHtml);

            //波特率
            let baudRateList = data.data.baudRateList;
            let baudRateHtml = "";
            $.each(baudRateList, function (index, item) {
                baudRateHtml += " <option value='" + item.value + "'>" + item.value + "</option>";
            });
            $("select[name='baudRate']").append(baudRateHtml);

            //数据位
            let dataBitList = data.data.dataBitList;
            let dataBitHtml = "";
            $.each(dataBitList, function (index, item) {
                dataBitHtml += " <option value='" + item.value + "'>" + item.value + "</option>";
            });
            $("select[name='dataBit']").append(dataBitHtml);

            //停止位
            let stopBitList = data.data.stopBitList;
            let stopBitHtml = "";
            $.each(stopBitList, function (index, item) {
                stopBitHtml += " <option value='" + item.value + "'>" + item.value + "</option>";
            });
            $("select[name='stopBit']").append(stopBitHtml);

            //校验位
            let checkDigitList = data.data.checkDigitList;
            let checkDigitHtml = "";
            $.each(checkDigitList, function (index, item) {
                checkDigitHtml += " <option value='" + item.value + "'>" + item.value + "</option>";
            });
            $("select[name='checkDigit']").append(checkDigitHtml);

            //传感器型号
            let sensorModelList = data.data.sensorList;
            let sensorModelHtml = "";
            $.each(sensorModelList, function (index, item) {
                sensorModelHtml += " <option value='" + item.sensorName + "'>" + item.sensorName + "</option>";
            });
            $("select[name='sensorModel']").append(sensorModelHtml);

            //反选
            // $("select[name='???']").val($("#???").val());
            //append后必须从新渲染
            form.render('select');
        }
    })


    //监听提交
    form.on('submit(comFormAdd)', function (data) {

        // layer.msg(JSON.stringify(data.field));
        let s = JSON.stringify(data.field);
        debugger
        //这里请求后台
        $.ajax({
            url: "/com/comDataAdd",
            data: s,
            dataType: 'JSON',
            contentType: "application/json",
            type: 'POST',
            success: function (result) {
                layer.msg(result.msg)
            }
        });


        return false;
    });


    exports('comAdd', {}); //注意，这里是模块输出的核心，模块名必须和use时的模块名一致
});









