//Ficheiro para criar os aliens, desenhar os aliens, fazer a logica do movimento do grupo de aliens e dos tiros dos aliens
import pt.isel.canvas.*

const val ALIEN_WIDTH = 56
const val ALIEN_HEIGHT = 40
const val ALIEN_COLUMNS = 11
const val ALIENS_START_Y = 40     //a primeira linha de aliens começa a 40 pixeis do topo do Canvas
const val ALIEN_SHIFT_X = 4       //em cada movimento os aliens andam 4 pixeis para o lado
const val ALIEN_SHIFT_Y = 20      //quando chegam ao limite do Canvas os aliens descem 20 pixeis
const val ALIEN_IMAGE_WIDTH = 112     //cada alien na imagem invaders.png é um retangulo de 112 x 80
const val ALIEN_IMAGE_HEIGHT = 80

enum class AlienType(val points: Int) { Squid(30), Crab(20), Octopus(10) }     //os três tipos de alien e os pontos que cada um vale quando é destruído
data class Alien(val pos: Position, val type: AlienType, val pose: Boolean)    //data class do alien, a pose diz qual das duas imagens do alien é desenhada
data class Aliens(val list: List<Alien>, val moveRight: Boolean)               //data class do grupo de aliens, moveRight é true quando o grupo anda para a direita e false quando anda para a esquerda

//Função que cria uma linha de aliens do mesmo tipo, lado a lado, y é a posição da linha no Canvas
fun createRow(type: AlienType, y: Int): List<Alien> {
    return (0..<ALIEN_COLUMNS).map { column ->
        val x = column * ALIEN_WIDTH      //cada alien fica ao lado do anterior
        Alien(pos = Position(x, y), type = type, pose = false)
    }
}

//Função que cria o grupo de aliens do inicio do jogo, 5 linhas de 11 aliens: duas de Squid, duas de Crab e uma de Octopus
fun createAliens(): Aliens {
    val squidRow1 = createRow(AlienType.Squid, ALIENS_START_Y)                         //linha 1, a de cima
    val squidRow2 = createRow(AlienType.Squid, ALIENS_START_Y + ALIEN_HEIGHT)           //linha 2
    val crabRow1 = createRow(AlienType.Crab, ALIENS_START_Y + 2 * ALIEN_HEIGHT)         //linha 3
    val crabRow2 = createRow(AlienType.Crab, ALIENS_START_Y + 3 * ALIEN_HEIGHT)         //linha 4
    val octopusRow = createRow(AlienType.Octopus, ALIENS_START_Y + 4 * ALIEN_HEIGHT)    //linha 5, a de baixo
    val list = squidRow1 + squidRow2 + crabRow1 + crabRow2 + octopusRow
    return Aliens(list = list, moveRight = true)     //no inicio os aliens andam para a direita
}

//Função para desenhar um alien, a imagem invaders.png tem uma linha por tipo de alien e duas colunas, uma por pose
fun Canvas.drawAlien(a: Alien) {     //a de alien
    val imageX = if (a.pose) ALIEN_IMAGE_WIDTH else 0           //a primeira pose está na coluna da esquerda da imagem e a segunda na da direita
    val imageY = a.type.ordinal * ALIEN_IMAGE_HEIGHT           //ordinal é a posição do tipo no enum (Squid = 0, Crab = 1, Octopus = 2), cada tipo está numa linha diferente da imagem
    drawImage("invaders|$imageX,$imageY,$ALIEN_IMAGE_WIDTH,$ALIEN_IMAGE_HEIGHT", a.pos.x, a.pos.y, ALIEN_WIDTH, ALIEN_HEIGHT)    //o texto antes do | é a imagem, e os 4 numeros a seguir são o retangulo da imagem que é desenhado
}

//Função que verifica se o grupo de aliens chegou ao limite lateral do Canvas, no lado para onde está a andar
fun Aliens.atBorder(area: Area): Boolean {
    val rightEdge = list.maxOf { it.pos.x + ALIEN_WIDTH }     //o lado direito do alien que está mais à direita
    val leftEdge = list.minOf { it.pos.x }                   //o lado esquerdo do alien que está mais à esquerda
    return if (moveRight)
        rightEdge + ALIEN_SHIFT_X > area.width     //se andam para a direita, chegaram ao limite se o próximo movimento passar do lado direito
    else
        leftEdge - ALIEN_SHIFT_X < 0               //se andam para a esquerda, chegaram ao limite se o próximo movimento passar do lado esquerdo
}

//Função que move o grupo de aliens: andam 4 pixeis para o lado ou, se chegaram ao limite, descem 20 pixeis e trocam de sentido
fun Aliens.move(area: Area): Aliens {
    val border = this.atBorder(area)
    val dx = if (border) 0 else if (moveRight) ALIEN_SHIFT_X else -ALIEN_SHIFT_X     //no limite os aliens não andam para o lado, só descem
    val dy = if (border) ALIEN_SHIFT_Y else 0
    val movedList = list.map { it.copy(pos = Position(it.pos.x + dx, it.pos.y + dy), pose = !it.pose) }     //move todos os aliens e troca a pose de cada um, para a imagem alternar a cada movimento
    return this.copy(list = movedList, moveRight = if (border) !moveRight else moveRight)                   //o sentido só muda quando chegam ao limite
}

//Função que verifica se algum alien chegou à linha da nave, ou seja, se a parte de baixo de algum alien já chegou ao topo da nave
fun Aliens.reachedShip(ship: SpaceShip): Boolean {
    return list.any { it.pos.y + ALIEN_HEIGHT >= ship.pos.y }
}

//Função que faz um alien disparar, o tiro aparece por baixo do alien e a meio dele
fun Alien.fire(): Shot {
    val shotX = pos.x + ALIEN_WIDTH / 2
    val shotY = pos.y + ALIEN_HEIGHT
    val speed = ALIEN_SHOT_MIN_SPEED..ALIEN_SHOT_MAX_SPEED     //a velociade vai entre 1 a 4 pixeis, como estava no trabalho anterior
    return Shot(pos = Position(shotX, shotY), vel = Velocity(0, speed.random()), width = SHOT_WIDTH, height = SHOT_HEIGHT, color = RED)
}