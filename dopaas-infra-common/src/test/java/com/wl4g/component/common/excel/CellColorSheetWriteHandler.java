package com.wl4g.component.common.excel;

import com.alibaba.excel.metadata.CellData;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.util.StyleUtil;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import java.util.List;

public class CellColorSheetWriteHandler implements CellWriteHandler {

	// 操作行
	private List<Integer> columnIndexs;
	// 操作列
	private List<Integer> rowIndexs;
	// 颜色
	private Short colorIndex;

	public CellColorSheetWriteHandler() {
	}

	public CellColorSheetWriteHandler(List<Integer> rowIndexs, List<Integer> columnIndexs, Short colorIndex) {
		this.rowIndexs = rowIndexs;
		this.columnIndexs = columnIndexs;
		this.colorIndex = colorIndex;
	}

	@Override
	public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head,
			Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {
		System.out.println("beforeCellCreate " + writeSheetHolder.getClazz());
	}

	@Override
	public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head,
			Integer relativeRowIndex, Boolean isHead) {
		System.out.println("afterCellCreate " + writeSheetHolder.getClazz());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
			List<CellData> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
		// 不处理第一行
		if (0 != cell.getRowIndex()) {
			// 根据单元格获取workbook
			Workbook workbook = cell.getSheet().getWorkbook();
			// 设置行高
			writeSheetHolder.getSheet().getRow(cell.getRowIndex()).setHeight((short) (1.4 * 256));
			// 单元格策略
			WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
			// 设置背景颜色白色
			contentWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
			// 设置垂直居中为居中对齐
			contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			// 设置左右对齐为靠左对齐
			contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
			// 设置单元格上下左右边框为细边框
			contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
			contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
			contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
			contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
			// 创建字体实例
			WriteFont cellWriteFont = new WriteFont();
			// 设置字体大小
			cellWriteFont.setFontHeightInPoints((short) 12);
			if (CollectionUtils.isNotEmpty(columnIndexs) && CollectionUtils.isNotEmpty(rowIndexs) && colorIndex != null
					&& rowIndexs.contains(cell.getRowIndex()) && columnIndexs.contains(cell.getColumnIndex())) {
				// 设置指定单元格字体颜色
				cellWriteFont.setColor(colorIndex);
			}
			contentWriteCellStyle.setWriteFont(cellWriteFont);
			CellStyle cellStyle = StyleUtil.buildHeadCellStyle(workbook, contentWriteCellStyle);
			cell.setCellStyle(cellStyle);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void afterCellDataConverted(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, CellData cellData,
			Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
		// TODO Auto-generated method stub
		System.out.println("afterCellDataConverted... writeSheetHolder: " + writeSheetHolder + ", cell: " + cell);
	}

}
