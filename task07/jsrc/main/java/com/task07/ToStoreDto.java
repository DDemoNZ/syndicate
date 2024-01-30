package com.task07;

import java.util.List;

public class ToStoreDto {

    private List<String> ids;

    public ToStoreDto(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
