define(['echarts'], function (echarts) {

       // 基于准备好的dom，初始化echarts实例
        var myChart = echarts.init(document.getElementById('profit_chart'));

        // 指定图表的配置项和数据
        var option = {
            tooltip : {
                trigger: 'axis',
                axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                    type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            legend: {
                data:['客户应收','订舱应付','拖车应付','驳船应付','报关应付','文档处理应付','仓储应付','保险应付','利润']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis : [
                {
                    type : 'category',
                    data : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月']
                }
            ],
            yAxis : [
                {
                    type : 'value'
                }
            ],
            series : [
                {
                    name:'客户应收',
                    type:'bar',
                    data:[320, 332, 301, 334, 390, 330, 320, 334, 390, 330, 320, 182]
                },
                {
                    name:'订舱应付',
                    type:'bar',
                    stack: '广告',
                    data:[120, 132, 101, 134, 90, 230, 210, 334, 390, 330, 320, 182]
                },
                {
                    name:'拖车应付',
                    type:'bar',
                    stack: '广告',
                    data:[220, 182, 191, 234, 290, 330, 310, 334, 390, 330, 320, 182]
                },
                {
                    name:'驳船应付',
                    type:'bar',
                    stack: '广告',
                    data:[150, 232, 201, 154, 190, 330, 410, 334, 390, 330, 320, 182]
                },
                {
                    name:'报关应付',
                    type:'bar',
                    data:[862, 1018, 964, 1026, 1679, 1600, 1570, 334, 390, 330, 320, 182],
                    markLine : {
                        lineStyle: {
                            normal: {
                                type: 'dashed'
                            }
                        },
                        data : [
                            [{type : 'min'}, {type : 'max'}]
                        ]
                    }
                },
                {
                    name:'文档处理应付',
                    type:'bar',
                    barWidth : 5,
                    stack: '搜索引擎',
                    data:[620, 732, 701, 734, 1090, 1130, 1120, 334, 390, 330, 320, 182]
                },
                {
                    name:'仓储应付',
                    type:'bar',
                    stack: '搜索引擎',
                    data:[120, 132, 101, 134, 290, 230, 220, 334, 390, 330, 320, 182]
                },
                {
                    name:'保险应付',
                    type:'bar',
                    stack: '搜索引擎',
                    data:[60, 72, 71, 74, 190, 130, 110, 334, 390, 330, 320, 182]
                },
                {
                    name:'利润',
                    type:'bar',
                    stack: '搜索引擎',
                    data:[62, 82, 91, 84, 109, 110, 120, 334, 390, 330, 320, 182]
                }
            ]
        };

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);

});