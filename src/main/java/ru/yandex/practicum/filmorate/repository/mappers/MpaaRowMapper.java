package ru.yandex.practicum.filmorate.repository.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpaa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaaRowMapper implements RowMapper<Mpaa> {
    @Override
    public Mpaa mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpaa mpaa = new Mpaa();
        mpaa.setId(rs.getInt("id"));
        mpaa.setName(rs.getString("name"));
        return mpaa;
    }
}
