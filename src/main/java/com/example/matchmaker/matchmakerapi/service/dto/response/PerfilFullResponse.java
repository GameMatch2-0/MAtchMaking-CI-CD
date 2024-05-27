package com.example.matchmaker.matchmakerapi.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PerfilFullResponse {
    private Long idPerfil;
    private UsuarioInPerfilResponse usuario;
    private String username;
    private String biografia;
    private Float nota;
    private String orientacaoSexual;
//    private boolean procuraAmizade;
//    private boolean procuraNamoro;
//    private boolean procuraPlayer2;
    private boolean isPremium = false;
    private List<JogoInPerfilResponse> generosJogos;
    private List<InteresseFullResponse> interesseList;
    private List<ConsoleFullResponse> consoleList;
    private List<MidiaFullResponse> midiaList;
}
