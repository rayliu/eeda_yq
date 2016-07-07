package controllers.util;


public class PermissionConstant {
	//TODO: 以后这里应该是从数据库中读取动态的权限list	
	
	
	/*运输单操作权限*/
	public static final String PERMISSION_TO_LIST = "TransferOrder.list";
	public static final String PERMISSION_TO_CREATE = "TransferOrder.create";
	public static final String PERMISSION_TO_UPDATE = "TransferOrder.update";
	public static final String PERMISSION_TO_DELETE = "TransferOrder.delete";
	public static final String PERMISSION_TO_ADD_REVENUE = "TransferOrder.add_revenue";
	
	/*------------------------------------red---------------------------------*/
	/*调车单操作权限*/
	public static final String PERMISSION_PO_LIST = "PickupOrder.list";
	public static final String PERMISSION_PO_CREATE = "PickupOrder.create";
	public static final String PERMISSION_PO_UPDATE = "PickupOrder.update";
	/*public static final String PERMISSION_PO_DELETE = "PickupOrder.delete";*/
	public static final String PERMISSION_PO_ADD_COST = "PickupOrder.add_cost";
	public static final String PERMISSION_PO_COMPLETED ="PickupOrder.completed";
	//add transferOrder
	/*public static final String PERMISSION_PO_ADD_TO ="PickupOrder.add_to";*/
	
	/*发车单权限点*/
	public static final String PERMISSION_DO_LIST = "DepartOrder.list";
	public static final String PERMISSION_DO_CREATE = "DepartOrder.create";
	public static final String PERMISSION_DO_UPDATE = "DepartOrder.update";
	public static final String PERMISSION_DO_ADD_COST = "DepartOrder.add_cost";
	public static final String PERMISSION_DO_COMPLETED ="DepartOrder.completed";
	
	/*在途更新*/
	public static final String PERMISSION_OT_LIST = "Ontrip.list";
	public static final String PERMISSION_OT_UPDATE = "Ontrip.update";
	
	/*入库权限*/
	public static final String PERMISSION_WO_INLIST = "WarehouseOrder.inList";
	public static final String PERMISSION_WO_INCREATE = "WarehouseOrder.inCreate";
	public static final String PERMISSION_WO_INUPDATE = "WarehouseOrder.inUpdate";
	public static final String PERMISSION_WO_INCOMPLETED = "WarehouseOrder.inCompleted";
	/*出库权限*/
	public static final String PERMISSION_WO_OUTLIST = "WarehouseOrder.outList";
	public static final String PERMISSION_WO_OUTCREATE = "WarehouseOrder.outCreate";
	public static final String PERMISSION_WO_OUTUPDATE = "WarehouseOrder.outUpdate";
	public static final String PERMISSION_WO_OUTCOMPLETED = "WarehouseOrder.outCompleted";
	/*库存权限*/
	public static final String PERMSSION_II_LIST = "InventoryItem.list";
	
	/*配送单权限*/
	public static final String PERMSSION_DYO_LIST = "DeliveryOder.list";
	public static final String PERMSSION_DYO_UPDATE = "DeliveryOder.update";
	public static final String PERMSSION_DYO_CREATE = "DeliveryOder.create";
	public static final String PERMSSION_DYO_COMPLETED = "DeliveryOder.completed";
	public static final String PERMSSION_DYO_ADD_COST = "DeliveryOder.add_cost";
	
	/*配送单在图更新权限*/
	public static final String PERMSSION_DOM_LIST = "DeliveryOderMilestone.list";
	public static final String PERMSSION_DOM_COMPLETED = "DeliveryOderMilestone.completed";
	
	/*回单权限*/
	public static final String PERMSSION_RO_LIST = "ReturnOrder.list";
	public static final String PERMSSION_RO_UPDATE = "ReturnOrder.update";
	public static final String PERMSSION_RO_ADD_REVENUE = "ReturnOder.add_revenue";
	public static final String PERMSSION_RO_COMPLETED = "ReturnOrder.completed";
	/*图片相关*/
	public static final String PERMSSION_RO_IMGOPERATION = "ReturnOrder.imgoperation";//图片操作
	public static final String PERMSSION_RO_IMGVIEW = "ReturnOrder.imgview";//图片查看
	
	
	
	/*应收明细确认权限*/
	/*public static final String PERMSSION_CI_LIST = "ChargeItem.list";*/
	public static final String PERMSSION_CI_AFFIRM = "ChargeItem.affirm";
	
	/*应收对账单权限*/
	public static final String PERMSSION_CCO_LIST = "ChargeCheckOrder.list";
	public static final String PERMSSION_CCO_UPDATE = "ChargeCheckOrder.update";
	public static final String PERMSSION_CCO_CREATE = "ChargeCheckOrder.create";
	public static final String PERMSSION_CCO_AFFIRM = "ChargeCheckOrder.affirm";//确认
	
	/*应收开票申请权限 ChargePreInvoiceOrder*/
	public static final String PERMSSION_CPIO_LIST = "ChargePreInvoiceOrder.list";
	public static final String PERMSSION_CPIO_CREATE = "ChargePreInvoiceOrder.create";
	public static final String PERMSSION_CPIO_UPDATE = "ChargePreInvoiceOrder.update";
	public static final String PERMSSION_CPIO_DELETE = "ChargePreInvoiceOrder.delete";
//	public static final String PERMSSION_CPIO_APPROVAL = "ChargePreInvoiceOrder.approval";//审核
//	public static final String PERMSSION_CPIO_CONFIRMATION = "ChargePreInvoiceOrder.confirmation";//审批
	
	/*应收开票记录*/
	public static final String PERMSSION_CIO_LIST = "ChargeInvoiceOrder.list";
	public static final String PERMSSION_CIO_CREATE = "ChargeInvoiceOrder.create";
	public static final String PERMSSION_CIO_UPDATE = "ChargeInvoiceOrder.update";
	public static final String PERMSSION_CIO_APPROVAL = "ChargeInvoiceOrder.approval";
	

	/*应付明细确认权限 CostItemConfirm*/
	/*public static final String PERMSSION_CTC_LIST = "CostItemConfirm.list";*/
	public static final String PERMSSION_CTC_AFFIRM = "CostItemConfirm.affirm";
	
	/*应付对账单*/
	public static final String PERMSSION_CCOI_LIST = "CostCheckOrder.list";
	public static final String PERMSSION_CCOI_CREATE = "CostCheckOrder.create";
	public static final String PERMSSION_CCOI_UPDATE = "CostCheckOrder.update";
	public static final String PERMSSION_CCOI_AFFIRM = "CostCheckOrder.affirm";
	
	/*付款申请 CostPreInvoiceOrder*/
	public static final String PERMSSION_CPO_LIST = "CostPreInvoiceOrder.list";
	public static final String PERMSSION_CPO_CREATE = "CostPreInvoiceOrder.create";
	public static final String PERMSSION_CPO_UPDATE = "CostPreInvoiceOrder.update";
	public static final String PERMSSION_CPO_APPROVAL = "CostPreInvoiceOrder.approval";
	public static final String PERMSSION_CPO_CONFIRMATION = "CostPreInvoiceOrder.confirmation";
	
	/*预付单 PrePayOrder*/
	public static final String PERMSSION_PrePayOrder_LIST = "PrePayOrder.list";
	public static final String PERMSSION_PrePayOrder_CREATE = "PrePayOrder.create";
	public static final String PERMSSION_PrePayOrder_UPDATE = "PrePayOrder.update";
	public static final String PERMSSION_PrePayOrder_CANCEL = "PrePayOrder.cancel";
	/*转账单 PrePayOrder*/
	public static final String PERMSSION_TA_LIST = "TransferAccountsOrder.list";
	public static final String PERMSSION_TA_CREATE = "TransferAccountsOrder.create";
	public static final String PERMSSION_TA_UPDATE = "TransferAccountsOrder.update";
	public static final String PERMSSION_TA_CANCEL = "TransferAccountsOrder.cancel";
	
	
	/*出纳日记账权限*/
	public static final String PERMSSION_PCO_LIST = "PaymentCheckOrder.list";
	
	/*客户合同权限*/
	public static final String PERMSSION_CC_LIST = "ContractCustomer.list";
	public static final String PERMSSION_CC_CREATE = "ContractCustomer.create";
	public static final String PERMSSION_CC_UPDAET = "ContractCustomer.update";
	public static final String PERMSSION_CC_DELETE = "ContractCustomer.delete";
	
	/*干线供应商合同权限*/
	public static final String PERMSSION_CP_LIST = "ContractProvider.list";
	public static final String PERMSSION_CP_CREATE = "ContractProvider.create";
	public static final String PERMSSION_CP_UPDATE = "ContractProvider.update";
	public static final String PERMSSION_CP_DELETE = "ContractProvider.delete";
	
	/*配送供应商合同权限*/
	public static final String PERMSSION_CD_LIST = "ContractDelivery.list";
	public static final String PERMSSION_CD_CREATE = "ContractDelivery.create";
	public static final String PERMSSION_CD_UPDATE = "ContractDelivery.update";
	public static final String PERMSSION_CD_DELETE = "ContractDelivery.delete";
	
	/*登录用户的权限*/
	public static final String PERMSSION_U_LIST = "User.list";
	public static final String PERMSSION_U_CREATE = "User.create";
	public static final String PERMSSION_U_UPDATE = "User.update";
	public static final String PERMSSION_U_DELETE = "User.delete";
	
	/*角色权限*/
	public static final String PERMSSION_R_LIST = "Role.list";
	public static final String PERMSSION_R_CREATE = "Role.create";
	public static final String PERMSSION_R_UPDATE = "Role.update";
	public static final String PERMSSION_R_DELETE = "Role.delete";
	
	/*用户角色权限*/
	public static final String PERMSSION_UR_LIST = "UserRole.list";
	public static final String PERMSSION_UR_CREATE = "UserRole.create";
	public static final String PERMSSION_UR_UPDATE = "UserRole.update";
	public static final String PERMSSION_UR_PERMISSION_LIST = "UserRole.permission_list";
	
	/*角色权限权限*/
	public static final String PERMSSION_RP_LIST = "RolePermission.list";
	public static final String PERMSSION_RP_CREATE = "RolePermission.create";
	public static final String PERMSSION_RP_UPDATE = "RolePermission.update";

	/*客户权限*/
	public static final String PERMSSION_C_LIST = "Customer.list";
	public static final String PERMSSION_C_CREATE = "Customer.create";
	public static final String PERMSSION_C_UPDATE = "Customer.update";
	public static final String PERMSSION_C_DELETE = "Customer.delete";
	
	/*供应商权限*/
	public static final String PERMSSION_P_LIST = "Provider.list";
	public static final String PERMSSION_P_CREATE = "Provider.create";
	public static final String PERMSSION_P_UPDATE = "Provider.update";
	public static final String PERMSSION_P_DELETE = "Provider.delete";
	
	/*供应商车辆权限*/
	public static final String PERMSSION_PC_LIST = "ProviderCar.list";
	public static final String PERMSSION_PC_CREATE = "ProviderCar.create";
	public static final String PERMSSION_PC_UPDATE = "ProviderCar.update";
	public static final String PERMSSION_PC_DELETE = "ProviderCar.delete";
	
	/*供应商司机信息权限*/
	public static final String PERMSSION_PD_LIST = "ProviderDriver.list";
	public static final String PERMSSION_PD_CREATE = "ProviderDriver.create";
	public static final String PERMSSION_PD_UPDATE = "ProviderDriver.update";
	public static final String PERMSSION_PD_DELETE = "ProviderDriver.delete";
	
	/*网点信息权限*/
	public static final String PERMSSION_O_LIST = "Office.list";
	public static final String PERMSSION_O_CREATE = "Office.create";
	public static final String PERMSSION_O_UPDATE = "Office.update";
	public static final String PERMSSION_O_DELETE = "Office.delete";
	
	/*产品信息权限*/
	public static final String PERMSSION_PT_LIST = "Product.list";
	public static final String PERMSSION_PT_CREATE = "Product.create";
	public static final String PERMSSION_PT_UPDATE = "Product.update";
	public static final String PERMSSION_PT_DELETE = "Product.delete";
	
	/*仓库信息权限*/
	public static final String PERMSSION_W_LIST = "Warehouse.list";
	public static final String PERMSSION_W_CREATE = "Warehouse.create";
	public static final String PERMSSION_W_UPDATE = "Warehouse.update";
	public static final String PERMSSION_W_DELETE = "Warehouse.delete";
	
	/*城市*/

	/*干线定义权限*/
	public static final String PERMSSION_RE_LIST = "Route.list";
	public static final String PERMSSION_RE_CREATE = "Route.create";
	public static final String PERMSSION_RE_UPDATE = "Route.update";
	public static final String PERMSSION_RE_DELETE = "Route.delete";
	
	/*里程碑*/

	/*收费条目*/
	public static final String PERMSSION_T_LIST = "Toll.list";
	public static final String PERMSSION_T_CREATE = "Toll.create";
	public static final String PERMSSION_T_UPDATE = "Toll.update";
	public static final String PERMSSION_T_DELETE = "Toll.delete";
	
	/*付费条目权限*/
	public static final String PERMSSION_PAY_LIST = "Pay.list";
	public static final String PERMSSION_PAY_CREATE = "Pay.create";
	public static final String PERMSSION_PAY_UPDATE = "Pay.update";
	public static final String PERMSSION_PAY_DELETE = "Pay.delete";

	/*金融账户信息权限*/
	public static final String PERMSSION_A_LIST = "Account.list";
	public static final String PERMSSION_A_CREATE = "Account.create";
	public static final String PERMSSION_A_UPDATE = "Account.update";
	public static final String PERMSSION_A_DELETE = "Account.delete";
	
	/*自营车辆信息权限*/
	public static final String PERMSSION_CI_LIST = "CarInfo.list";
	public static final String PERMSSION_CI_CREATE = "CarInfo.create";
	public static final String PERMSSION_CI_UPDATE = "CarInfo.update";
	public static final String PERMSSION_CI_DELETE = "CarInfo.delete";
	
	/*自营司机信息*/
	public static final String PERMSSION_D_LIST = "Driver.list";
	public static final String PERMSSION_D_CREATE = "Driver.create";
	public static final String PERMSSION_D_UPDATE = "Driver.update";
	public static final String PERMSSION_D_DELETE = "Driver.delete";
	
	/*行车单权限 CarSummary*/
	public static final String PERMSSION_CS_LIST = "CarSummary.list";
	public static final String PERMSSION_CS_CREATE = "CarSummary.create";
	public static final String PERMSSION_CS_UPDATE = "CarSummary.update";
	public static final String PERMSSION_CS_APPROVAL = "CarSummary.approval";
	
	/*保险单权限.InsuranceOrder*/
	public static final String PERMSSION_IO_LIST = "InsuranceOrder.list";
	public static final String PERMSSION_IO_CREATE = "InsuranceOrder.create";
	public static final String PERMSSION_IO_UPDATE = "InsuranceOrder.update";
	/*收款确认*/
	public static final String PERMSSION_COLLECTIONCONFIRM_LIST = "chargeAcceptOrder.list"; 
	public static final String PERMSSION_COLLECTIONCONFIRM_CONFIRM = "chargeAcceptOrder.confirm"; 
	/*付款确认*/
	public static final String PERMSSION_COSTCONFIRM_LIST = "costAcceptOrder.list"; 
	public static final String PERMSSION_COSTCONFIRM_CONFIRM = "costAcceptOrder.confirm";
	/*报销单*/
	public static final String PERMSSION_COSTREIMBURSEMENT_LIST = "costReimbureement_list";
	public static final String PERMSSION_COSTREIMBURSEMENT_CREATE = "costReimbureement_create";
	public static final String PERMSSION_COSTREIMBURSEMENT_UPDATE = "costReimbureement_update";
	public static final String PERMSSION_COSTREIMBURSEMENT_CONFIRM = "costReimbureement_confirm";
	public static final String PERMSSION_COSTREIMBURSEMENT_ALLDATA = "costReimbureement_alldata";
	/*报表查询权限点*/
	public static final String PERMSSION_PRODUCTINDEX_LIST = "ProductIndex.list";
	public static final String PERMSSION_ORDERINDEX_LIST = "OrderIndex.list";
	public static final String PERMSSION_DAILYREPORT_LIST = "DailyReport.list";
	public static final String PERMSSION_ORDER_FLOW_LIST = "OrderFlowReport.list";
	/*往来票据权限点*/
	public static final String PERMSSION_IMO_LIST = "InOutMiscOrder.list";
	public static final String PERMSSION_IMO_CREATE = "InOutMiscOrder.create";
	public static final String PERMSSION_IMO_UPDATE = "InOutMiscOrder.update";
	public static final String PERMSSION_IMO_DELETE = "InOutMiscOrder.delete";
	
	public static final String costQueryReport ="costQueryReport";
	public static final String revenueQueryReport ="revenueQueryReport";
	
	
}
