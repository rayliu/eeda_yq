define(['jquery', 'echarts'], function ($, ec) {
    var myChartContainer = document.getElementById('echarts');
    // 基于准备好的dom，初始化echarts实例
    var myChart = ec.init(myChartContainer);

    // 指定图表的配置项和数据
    var bar_option = {
        title: {
            text: '图表示例'
        },
        tooltip: {},
        legend: {
            data:['销量']
        },
        xAxis: {
            data: ["衬衫","羊毛衫","雪纺衫","裤子","高跟鞋","袜子"]
        },
        yAxis: {},
        series: [{
            name: '销量',
            type: 'bar',
            data: [5, 20, 36, 10, 10, 20]
        }]
    };

    line_option = {
        title: {
            text: '图表示例'
        },
        xAxis: {
            type: 'category',
            data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        },
        yAxis: {
            type: 'value'
        },
        series: [{
            data: [820, 932, 901, 934, 1290, 1330, 1320],
            type: 'line'
        }]
    };

    pie_option = {
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b}: {c} ({d}%)"
        },
        legend: {
            orient: 'vertical',
            x: 'left',
            data:['直接访问','邮件营销','联盟广告','视频广告','搜索引擎']
        },
        series: [
            {
                name:'访问来源',
                type:'pie',
                radius: ['50%', '70%'],
                avoidLabelOverlap: false,
                label: {
                    normal: {
                        show: false,
                        position: 'center'
                    },
                    emphasis: {
                        show: true,
                        textStyle: {
                            fontSize: '30',
                            fontWeight: 'bold'
                        }
                    }
                },
                labelLine: {
                    normal: {
                        show: false
                    }
                },
                data:[
                    {value:335, name:'直接访问'},
                    {value:310, name:'邮件营销'},
                    {value:234, name:'联盟广告'},
                    {value:135, name:'视频广告'},
                    {value:1548, name:'搜索引擎'}
                ]
            }
        ]
    };

    scatter_option = {
        title: {
            text: '图表示例'
        },
        xAxis: {},
        yAxis: {},
        series: [{
            symbolSize: 20,
            data: [
                [10.0, 8.04],
                [8.0, 6.95],
                [13.0, 7.58],
                [9.0, 8.81],
                [11.0, 8.33],
                [14.0, 9.96],
                [6.0, 7.24],
                [4.0, 4.26],
                [12.0, 10.84],
                [7.0, 4.82],
                [5.0, 5.68]
            ],
            type: 'scatter'
        }]
    };
    // 使用刚指定的配置项和数据显示图表。
    myChart.setOption(bar_option);

    $('#chart_type').change(function(e){
        var code=$(this).children('option:selected').val();
        if(code=='bar'){
            myChart.setOption(bar_option,{notMerge:true});
        }else if(code=='line'){
            myChart.setOption(line_option,{notMerge:true});
        }else if(code=='pie'){
            myChart.setOption(pie_option,{notMerge:true});
        }else if(code=='scatter'){
            myChart.setOption(scatter_option,{notMerge:true});
        }
    });

    //https://blog.csdn.net/u012043416/article/details/51912011
    //用于使chart自适应高度和宽度,通过窗体高宽计算容器高宽
    var resizeMyChartContainer = function () {
        myChartContainer.style.width = myChartContainer.clientWidth+'px';
        myChartContainer.style.height = myChartContainer.clientHeight+'px';
    };
    //设置容器高宽
    resizeMyChartContainer();

    //用于使chart自适应高度和宽度
    window.onresize = function () {
        //重置容器高宽
        resizeMyChartContainer();
        myChart.resize();
    };

    var buildDetail=function(){
        var dto = {
            chart_type: $('#chart_type').val()
        }
        return dto;
    }
    var display=function(data){
        if(data){
            $('#chart_id').val(data.ID);
            $('#chart_type').val(data.CHART_TYPE).change();
        }
    }
    return {
        buildDetail: buildDetail,
        display:display
    };
});