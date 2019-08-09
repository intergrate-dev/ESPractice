// 正式环境
const amucUrl = "http://118.31.239.157:8080/",
    requestUrl = {
        querySiteInfo: amucUrl + "doc/querySiteInfo"
    },
    pageFreshTime = 60 * 60 * 1000;
// 测试
// const amucUrl = "http://118.31.239.157:8080/",
//     requestUrl = {
//         querySiteInfo: "../json/source.json"
//     },
//     pageFreshTime = 10 * 1000;

new Vue({
    el: "#source",
    data: {
        total: 0,
        timer: null,
        loading: false,
        tableData: [],
    },
    mounted: function () {
        let obj = this;
        obj.getTableDate(1);
        obj.autoScreen();
        window.onresize = function () {
            obj.autoScreen();
        }
        obj.timer = setInterval(function(){
            obj.getTableDate(1);
        },pageFreshTime);
    },
    methods: {
        // 按钮显示状态对应中文
        tagName(status) {
            let tagObj = {
                "1": "已更新",
                "0": "更新中",
                "-1": "未更新"
            }
            return tagObj[status];
        },
        // 按钮button的type属性值
        typeName(status) {
            let typeObj = {
                "1": "success",
                "0": "primary",
                "-1": "danger"
            }
            return typeObj[status];
        },
        // 按钮图标的属性值
        iconName(status) {
            let iconObj = {
                "1": "el-icon-success",
                "0": "el-icon-refresh",
                "-1": "el-icon-error"
            }
            return iconObj[status];
        },
        // 筛选标签方法
        filterTag(value, row) {
            return row.status === value;
        },
        filterHandler(value, row, column) {
            const property = column['property'];
            return row[property] === value;
        },
        // 获取表格内容
        getTableDate: function (pageNo) {
            let obj = this;
            $.ajax({
                url: requestUrl.querySiteInfo,
                data: { "pageNo": pageNo, "limit": 10 },
                type: "POST",
                dataType: "JSON",
                success: function (data) {
                    if (data.status == 200) {
                        obj.loading = false;
                        obj.total = data.resultObject.totalCount;
                        obj.tableData = data.resultObject.list;
                    } else {
                        obj.loading = false;
                        console.log(data.errorMessage)
                    }
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    // 状态码
                    console.log(XMLHttpRequest.status);
                    // 状态
                    console.log(XMLHttpRequest.readyState);
                    // 错误信息
                    console.log(textStatus);
                }
            });
        },
        // 切换页数方法
        handleCurrentChange(val) {
            let obj = this;
            obj.loading = true;
            obj.getTableDate(val)
            // console.log(`当前页: ${val}`);
        },
        // 自适应屏幕方法
        autoScreen:function() {
            // 2.获取要改变的元素的宽高及屏幕的宽高，计算出缩放比例
            const targetPannel = document.getElementsByClassName("page")[0];
            if (targetPannel) {
                const webWidth = targetPannel.clientWidth,
                    webHeight = targetPannel.clientHeight;
        
                let showWidth = 0,
                    showHeight = 0,
                    preWidth = 0,
                    preHeight = 0;
                // 获取要显示的区域宽高---包含滚动条宽高
                showWidth = window.innerWidth;
                showHeight = window.innerHeight;
                // alert("wisth:" + showWidth + ", height:" + showHeight);
        
                // 计算缩放比例
                preWidth = showWidth / webWidth;
                preHeight = showHeight / webHeight;
        
                targetPannel.style.setProperty("transform", "scale(" + preWidth + ", " + preHeight + ")", 'important');
                targetPannel.style.setProperty("transform-origin", "0px 0px", 'important');
                // 防止body出现滚动条
                // document.body.style.cssText = "width:" + showWidth + "px; height:" + showHeight + "px;overflow:hidden;";
        
        
            } else {
                console.log("无page元素");
            }
        
        }
    }
});