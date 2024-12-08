package org.jcvalram.appmockito.repositories;

import org.jcvalram.appmockito.models.Examen;

import java.util.List;

public interface ExamenRepository {
    Examen guardar(Examen examen);
    List<Examen> findAll();
}
