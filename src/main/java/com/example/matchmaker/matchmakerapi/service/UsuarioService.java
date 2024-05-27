package com.example.matchmaker.matchmakerapi.service;

import com.example.matchmaker.matchmakerapi.api.configuration.security.jwt.GerenciadorTokenJwt;
import com.example.matchmaker.matchmakerapi.entity.Usuario;
import com.example.matchmaker.matchmakerapi.entity.repository.UsuarioRepository;
import com.example.matchmaker.matchmakerapi.service.authentication.dto.UsuarioLoginDto;
import com.example.matchmaker.matchmakerapi.service.authentication.dto.UsuarioTokenDto;
import com.example.matchmaker.matchmakerapi.service.dto.request.UsuarioRequest;
import com.example.matchmaker.matchmakerapi.service.dto.request.mapper.RequestMapper;
import com.example.matchmaker.matchmakerapi.service.dto.response.mapper.ResponseMapper;
import com.example.matchmaker.matchmakerapi.service.dto.response.UsuarioFullResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final GerenciadorTokenJwt gerenciadorTokenJwt;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;


    public UsuarioTokenDto autenticar(UsuarioLoginDto usuarioLoginDto){
        final UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
                usuarioLoginDto.getEmail(), usuarioLoginDto.getSenha()
        );

        final Authentication authentication = this.authenticationManager.authenticate(credentials);

        Usuario usuarioAutenticado =
                this.usuarioRepository.findByEmailAndDeletedFalse(usuarioLoginDto.getEmail())
                        .orElseThrow(
                                () -> new ResponseStatusException(404, "Email do usuario não encontrado", null)
                        );
        this.usuarioRepository.save(usuarioAutenticado);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = gerenciadorTokenJwt.generateToken(authentication);

        return RequestMapper.toUsuarioTokenDto(usuarioAutenticado, token);
    }




    public Usuario salvar(Usuario usuario) {
        return this.usuarioRepository.save(usuario);
    }

    public Usuario criar(UsuarioRequest usuarioRequest) {
        final Usuario novoUsuario = RequestMapper.toUsuario(usuarioRequest);
        if (buscarPorEmail(novoUsuario.getEmail()).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        String senhaCriptografada = passwordEncoder.encode(novoUsuario.getSenha());
        novoUsuario.setSenha(senhaCriptografada);

        this.usuarioRepository.save(novoUsuario);
        return novoUsuario;
    }


    public List<UsuarioFullResponse> listar() {
        List<Usuario> usuarioList = this.usuarioRepository.findAllByDeletedFalse();

        return usuarioList.stream()
                .map(ResponseMapper::toUsuarioFullResponse)
                .collect(Collectors.toList());
    }

    public List<Usuario> listarUsuariosParaCsv() {
        List<Usuario> usuarioList = this.usuarioRepository.findAllByDeletedFalse();

        return usuarioList;
    }

    public List<UsuarioFullResponse> listarApagados() {
        List<Usuario> usuarioList = this.usuarioRepository.findAllByDeletedTrue();

        return usuarioList.stream()
                .map(ResponseMapper::toUsuarioFullResponse)
                .collect(Collectors.toList());
    }


    public Usuario buscarPorId(String id) {
        Usuario usuario = this.usuarioRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não encontrado")
        );

        return usuario;
    }

    public Optional<Usuario> buscarPorNome(String nome) {
        return this.usuarioRepository.findByNomeIgnoreCaseAndDeletedFalse(nome);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return this.usuarioRepository.findByEmailAndDeletedFalse(email);
    }

    public Usuario atualizar(String id, UsuarioRequest usuarioRequest) {
        this.usuarioRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")
        );

        Usuario updateUsuario = RequestMapper.toUsuario(usuarioRequest);
        return salvar(updateUsuario);
    }

    public void deletar(String id) {
        Usuario usuario = this.usuarioRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não encontrado")
        );
        usuario.setDeleted(true);
        salvar(usuario);
    }

    public Usuario buscarUsuarioPorIdPerfil(Long idPerfil) {
        return this.usuarioRepository.findByPerfil(idPerfil).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario não encontrado")
        );
    }

    public boolean validaUsuario(UsuarioRequest usuario) {
        return usuario.getNome() != null && !usuario.getNome().isEmpty()
                && usuario.getIdentidadeGenero() != null
                && !usuario.getIdentidadeGenero().isEmpty() && usuario.getEmail() != null
                && !usuario.getEmail().isEmpty() && usuario.getEmail().length() >= 3
                && usuario.getEmail().contains("@") && usuario.getSenha() != null
                && !usuario.getSenha().isEmpty() && usuario.getDtNascimento() != null
                && !usuario.getDtNascimento().toString().isEmpty();

    }

    public boolean existsById(String id) {
        return this.usuarioRepository.existsById(id);
    }

    public static void gravaRegistro(String registro, String nomeArq) {
        BufferedWriter saida = null;

        // Bloco try-catch para abrir o arquivo
        try {
            saida = new BufferedWriter(new FileWriter(nomeArq, true));
        }
        catch (IOException erro) {
            System.out.println("Erro ao abrir o arquivo");
        }

        // Bloco try-catch para gravar o registro e fechar o arquivo
        try {
            saida.append(registro + "\n");
            saida.close();
        }
        catch (IOException erro) {
            System.out.println("Erro ao gravar o arquivo");
            erro.printStackTrace();
        }
    }

    public static void gravaArquivoTxt(List<Usuario> lista, String nomeArq) {
        int contaRegDadosGravados = 0;

        // Monta o registro de header
        String header = String.format("00USER%s01", LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMM-yyyy HH:mm:ss")));

        // Grava o header
        gravaRegistro(header, nomeArq);

        // Monta e grava os registros de dados (registros de corpo)
        for (Usuario u : lista) {
            String corpo = String.format("02%9s%-45.45s%-100.100s%-100.100s%-45.45s%s%s%s%01d",
                    u.getId(), u.getNome(), u.getSobrenome(), u.getEmail(), u.getCelular(),
                    u.getDtNascimento(), u.getDtCadastro(), u.getIdentidadeGenero(), u.isDeleted() ? 1 : 0);

            // Grava o registro de corpo
            gravaRegistro(corpo, nomeArq);

            // Contabiliza a quantidade de registros de dados gravados
            contaRegDadosGravados++;
        }

        // Monta e grava o registro de trailer
        String trailer = String.format("01%09d", contaRegDadosGravados);
        gravaRegistro(trailer, nomeArq);
    }

    public void gravaArquivoCsv(List<Usuario> lista, String nomeArq) {
        if (lista.isEmpty()) {
            System.out.println("A lista está vazia. Não há nada para gravar");
            return;
        } else {
            FileWriter arq = null; // Representa o arquivo que será gravado
            Formatter saida = null; // Objeto que será usado para escrever no arquivo
            Boolean deuRuim = false; // Flag para indicar se deu erro

            nomeArq += ".csv";

            // Criando o arquivo
            try {
                arq = new FileWriter(nomeArq, false); // Abre o arquivo
                saida = new Formatter(arq); // Instancia o obj saida, associando-o ao arquivo saida
            } catch (IOException erro) {
                System.out.println("Erro ao abrir o arquivo");
                System.exit(1);
            }

            // Gravando os objetos no arquivo
            try {
                // Percorre a lista, escrevendo cada objeto no arquivo e gravando um registro para cada Cachorro
                for (int i = 0; i < lista.size(); i++) {
                    // Instancia um objeto Cachorro para receber cada elemento da lista
                    Usuario user= lista.get(i);
                    saida.format("%s;%s;%s;%s;%s;%s;%s;%b;%b\n", user.getId(), user.getNome(), user.getSobrenome(), user.getDtNascimento(), user.getEmail(), user.getCelular(), user.getDtCadastro(), user.isDeleted());
                }
            } catch (FormatterClosedException erro) {
                System.out.println("Erro ao gravar no arquivo");
                erro.printStackTrace();
                deuRuim = true;
            } finally {
                saida.close(); // Fecha o arquivo
                try {
                    arq.close(); // Fecha o arquivo
                } catch (IOException err) {
                    System.out.println("Erro ao fechar o arquivo");
                    deuRuim = true;
                }
                if (deuRuim) {
                    System.exit(1);
                }
            }
        }
    }
}
