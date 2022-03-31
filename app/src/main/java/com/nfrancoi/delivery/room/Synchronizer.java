package com.nfrancoi.delivery.room;

import com.nfrancoi.delivery.room.dao.BaseDao;
import com.nfrancoi.delivery.room.entities.BaseEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Synchronizer<Entity extends BaseEntity> {


    private final BaseDao<Entity> dao;


    public Synchronizer(BaseDao<Entity> dao) {
        this.dao = dao;
    }

    //
    // logger
    //
    private String log;

    private List<String> logs = new LinkedList<>();

    public List<String> getLogs() {
        return logs;
    }

    private void addLog(Long entityId, String log) {

        logs.add("[" + entityId + "] " + log);
    }


    public void syncAll(List<Entity> toSyncEntities) {

        List<Entity> dbEntities = dao.getAllSync();

        dbEntities.stream().forEach(dbEntity -> {
            Entity foundToSyncEntity = toSyncEntities.stream()
                    .filter(toSyncEntity -> Objects.equals(toSyncEntity.getId(), dbEntity.getId()))
                    .findAny().orElse(null);
            //delete or desactivate
            if (foundToSyncEntity == null || (!foundToSyncEntity.isActive() && dbEntity.isActive())) {
                this.deleteSetInactive(dbEntity);
            }

        });

        //update or insert rows params
        try {
            toSyncEntities.stream().forEach(toSyncEntity -> {

                this.log = toSyncEntity.toString();

                Entity foundDbEntity = dbEntities.stream()
                        .filter(dbEntity -> Objects.equals(toSyncEntity.getId(), dbEntity.getId()))
                        .findAny().orElse(null);
                if (foundDbEntity == null || !foundDbEntity.equals(toSyncEntity)) {
                    this.updateInsert(toSyncEntity);
                } else {
                    this.addLog(toSyncEntity.getId(), "already uptodate");
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(log, e);
        }

    }

    private void deleteSetInactive(Entity dbEntity) {
        int numberOfDeleted = 0;
        try {
            numberOfDeleted = dao.deleteSync(dbEntity);
        } catch (Exception silentlyDiscard) {
            silentlyDiscard.printStackTrace();
        }

        if (1 != numberOfDeleted) {
            dbEntity.setActive(false);
            int nbrUpdate = dao.updateSync(dbEntity);
            if (nbrUpdate == 0) {
                this.addLog(dbEntity.getId(), "ERROR cannot update");
                throw new IllegalStateException("Cannot update entity");
            } else {
                this.addLog(dbEntity.getId(), "desactivated");
            }

        } else {
            this.addLog(dbEntity.getId(), "deleted");
        }
    }

    private void updateInsert(Entity entity) {
        int nbrUpdate = dao.updateSync(entity);
        if (nbrUpdate == 0) {
            Long id = dao.insertSync(entity);
            if (id < 1l) {
                this.addLog(entity.getId(), "ERROR cannot insert");
                throw new IllegalStateException("Cannot insert entity");
            } else {
                this.addLog(entity.getId(), "insert");
            }
        } else {
            this.addLog(entity.getId(), "updated");
        }
    }
}
