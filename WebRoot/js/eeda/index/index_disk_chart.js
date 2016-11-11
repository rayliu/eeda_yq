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

        var option = {
            title : {
                text: '磁盘空间',
                subtext: '本图不会自动刷新',
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{a} <br/>{b} : {c} ({d}%)"
            },
            series : [
                {
                    name: '磁盘空间',
                    type: 'pie',
                    radius : '55%',
                    center: ['50%', '60%'],
                    data:[
                        {value:335, name:'已使用'},
                        {value:310, name:'未使用'},
                    ],
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }
            ]
        };

        // setInterval(function () {
        //     randomData();
        // }, 1000);

        // 基于准备好的dom，初始化echarts实例
        var myChart = echarts.init(document.getElementById('disk_chart'));
        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);

});