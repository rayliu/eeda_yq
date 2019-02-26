define(['jquery', 'printMe', 'sco'], function ($) {

    $('#template_list_modal_ok_btn').click(function(event) {
        $('#print_template_list').modal('hide');

        var id=$("[name=template_id]:checked").val();
        var content = $('#template_content_'+id).html();

        $("#template_content").empty().append(content);
        $('#print_template').modal('show');
        
        
    });

    $('#ok_print_btn').click(function(event) {
        $("#template_content").printMe({"path": ["/css/print/eeda_print.css?v=2"]});

    });
});
