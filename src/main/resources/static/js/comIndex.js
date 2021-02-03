/**
 项目JS主入口
 以依赖layui的layer和form模块为例
 **/
layui.define(['layer', 'form','element'], function(exports){
    var  $ = layui.jquery,
        layer = layui.layer,
        form = layui.form,
        element = layui.element;//Tab的切换功能，切换事件监听等，需要依赖element模块


    //触发事件
    var active = {
        offset: function (othis) {
            var type = othis.data('type')
                , text = othis.text();


            layer.open({
                type: 2
                , id: 'layerDemo' + type //防止重复弹出
                , content: '/com/toAdd'
                , btn: '关闭'
                , btnAlign: 'c' //按钮居中
                , shade: 0.3 //不显示遮罩
                , area: ['100%', '100%']
                , maxWidth: 360
                , yes: function () {
                    layer.closeAll();
                }
                , success: function (layero, index) {
                    console.log(layero, index);
                }
            });
        }
    };

    $('#layerDemo .layui-btn').on('click', function () {
        var othis = $(this), method = othis.data('method');
        active[method] ? active[method].call(this, othis) : '';
    });




    exports('comIndex', {}); //注意，这里是模块输出的核心，模块名必须和use时的模块名一致
});










