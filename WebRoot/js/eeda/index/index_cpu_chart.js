define(['echarts'], function (echarts) {
        var data = [];

        function randomData() {
            var now = new Date();
            var value = Math.random() * 50+50;
            // console.log(now.toString());
            $.post('/sys/cpuUsage').done(function(backData){
                var obj={
                    name: now.toString(),
                    value: [
                        //x值
                        now.toString(),
                        //y值
                        backData
                    ]
                };
                //console.log(backData);
                data.push(obj);
                myChart.setOption({
                    series: [{
                        data: data
                    }]
                });
            });
        }

        //初始化
        
        // var now = new Date();
        // var oneDay = 24 * 3600 * 1000;
        // var oneSecond=1000;
        // var value = Math.random() * 50+50;
        // for (var i = 0; i < 1000; i++) {
        //     data.push(randomData());
        // }

        option = {
            title: {
                text: 'CPU%'
            },
            tooltip: {
                trigger: 'axis',
                formatter: function (params) {
                    params = params[0];
                    //console.log(params);
                    var date = new Date(params.name);
                    return params.value[1]+'%';
                },
                axisPointer: {
                    animation: false
                }
            },
            xAxis: {
                type: 'time',
                splitLine: {
                    show: false
                }
            },
            yAxis: {
                type: 'value',
                //boundaryGap: [0, '40%'],
                splitLine: {
                    show: false
                }
            },
            series: [{
                name: '模拟数据',
                type: 'line',
                showSymbol: false,
                hoverAnimation: false,
                data: data
            }]
        };

        setInterval(function () {
            randomData();
            // for (var i = 0; i < 5; i++) {
                // data.shift();
                // data.push(randomData());
            // }

            // myChart.setOption({
            //     series: [{
            //         data: data
            //     }]
            // });
        }, 1000);

        // 基于准备好的dom，初始化echarts实例
        var myChart = echarts.init(document.getElementById('cpu_chart'));
        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);

});