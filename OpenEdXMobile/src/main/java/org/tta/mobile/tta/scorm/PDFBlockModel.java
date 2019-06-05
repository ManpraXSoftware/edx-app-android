package org.tta.mobile.tta.scorm;

import org.tta.mobile.model.course.BlockModel;
import org.tta.mobile.model.course.CourseComponent;

public class PDFBlockModel extends ScormBlockModel {

    private ScormData data;

    public PDFBlockModel(BlockModel blockModel, CourseComponent parent) {
        super(blockModel, parent);
        this.data = (ScormData) blockModel.data;
    }

    public void setData(ScormData data) {
        this.data = data;
    }

}
